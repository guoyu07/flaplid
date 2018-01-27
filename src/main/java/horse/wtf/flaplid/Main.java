/*
 *  This file is part of flaplid.
 *
 *  flaplid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  flaplid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with flaplid.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.flaplid;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import horse.wtf.flaplid.checks.Check;
import horse.wtf.flaplid.checks.FatalCheckException;
import horse.wtf.flaplid.checks.aws.AWSIAMCheck;
import horse.wtf.flaplid.checks.aws.EC2SecurityGroupsCheck;
import horse.wtf.flaplid.checks.dns.DNSCheck;
import horse.wtf.flaplid.checks.github.GitHubOrganizationCheck;
import horse.wtf.flaplid.checks.slack.SlackTeamCheck;
import horse.wtf.flaplid.checks.supplychain.WebsiteDownloadCheck;
import horse.wtf.flaplid.checks.supplychain.WebsiteLinkTargetCheck;
import horse.wtf.flaplid.checks.supplychain.WebsiteRedirectCheck;
import horse.wtf.flaplid.configuration.CheckConfiguration;
import horse.wtf.flaplid.configuration.Configuration;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    private static final int FAILURE = 1;

    public static void main(String[] argv) {
        LOG.info("Starting up. (•_•) .. ( •_•)>⌐■-■ .. (⌐■_■)");
        Version version = new Version();
        LOG.info("Version: {}.", version.getVersionString());

        final CLIArguments cliArguments = new CLIArguments();

        // Parse CLI arguments.
        JCommander.newBuilder()
                .addObject(cliArguments)
                .build()
                .parse(argv);

        if (cliArguments.hasTags()) {
            LOG.info("Running all enabled checks matching any of the following tags: {}.", cliArguments.getTags());
        } else {
            LOG.info("No tags passed. Running all enabled checks.");
        }

        // Parse configuration.
        Configuration configuration = null;
        try {
            File file = new File(cliArguments.getConfigFilePath());
            CharSource source = Files.asCharSource(file, Charsets.UTF_8);

            ObjectMapper configObjectMapper = new ObjectMapper(new YAMLFactory());
            configuration = configObjectMapper.readValue(source.read(), Configuration.class);

            // Add additional configurations from included (like checks.d/) directory.
            if (configuration.include != null && !Strings.isNullOrEmpty(configuration.include)) {
                File includeFolder = new File(configuration.include);

                if (!includeFolder.isDirectory() || !includeFolder.canRead()) {
                    LOG.error("Include directory [{}] is not a directory or not readable. Terminating.", includeFolder.getCanonicalPath());
                    System.exit(FAILURE);
                }

                LOG.info("Including configuration from [{}].", includeFolder.getCanonicalPath());

                for (File includeFile : Files.fileTreeTraverser().breadthFirstTraversal(includeFolder)) {
                    if(includeFile.isFile()) {
                        if (includeFile.getName().endsWith(".yml")) {
                            LOG.info("Reading configuration from [{}].", includeFile.getCanonicalPath());
                            Configuration additional = configObjectMapper.readValue(
                                    Files.asCharSource(includeFile, Charsets.UTF_8).read(),
                                    Configuration.class
                            );

                            // Add all additional checks to the root configuration.
                            configuration.checks.addAll(additional.checks);
                        } else {
                            LOG.debug("Filename does not end with .yml and is ignored: [{}]", includeFile.getCanonicalPath());
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Could not read configuration file.", e);
            System.exit(FAILURE);
        }

        if (!configuration.isComplete()) {
            LOG.error("Configuration is incomplete. Please refer to the example configuration file.");
            System.exit(FAILURE);
        }

        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        ImmutableList.Builder<Check> disabledChecks = new ImmutableList.Builder<>();
        ImmutableList.Builder<Issue> issues = new ImmutableList.Builder<>();

        // Load all checks.
        Set<String> checkIDs = Sets.newHashSet();
        for (Map<String, Object> configMap : configuration.checks) {
            CheckConfiguration checkConfiguration = new CheckConfiguration(configMap, configuration);

            if(!checkConfiguration.standardParametersAreComplete()) {
                LOG.error("Missing attribute on check configuration. Skipping.");
                continue;
            }

            // Do not allow duplicate check IDs.
            if(checkConfiguration.isEnabled()) {
                if (checkIDs.contains(checkConfiguration.getId())) {
                    LOG.error("Duplicate check ID: [{}]. Terminating", checkConfiguration.getId());
                    System.exit(FAILURE);
                }

                checkIDs.add(checkConfiguration.getId());
            }

            Check check;
            switch (checkConfiguration.getType()) {
                case WebsiteDownloadCheck.TYPE:
                    check = new WebsiteDownloadCheck(checkConfiguration.getId(), checkConfiguration, httpClient);
                    break;
                case SlackTeamCheck.TYPE:
                    check = new SlackTeamCheck(checkConfiguration.getId(), checkConfiguration, httpClient, om);
                    break;
                case GitHubOrganizationCheck.TYPE:
                    check = new GitHubOrganizationCheck(checkConfiguration.getId(), checkConfiguration);
                    break;
                case EC2SecurityGroupsCheck.TYPE:
                    check = new EC2SecurityGroupsCheck(checkConfiguration.getId(), checkConfiguration);
                    break;
                case AWSIAMCheck.TYPE:
                    check = new AWSIAMCheck(checkConfiguration.getId(), checkConfiguration);
                    break;
                case WebsiteLinkTargetCheck.TYPE:
                    check = new WebsiteLinkTargetCheck(checkConfiguration.getId(), checkConfiguration);
                    break;
                case DNSCheck.TYPE:
                    check = new DNSCheck(checkConfiguration.getId(), checkConfiguration);
                    break;
                case WebsiteRedirectCheck.TYPE:
                     check = new WebsiteRedirectCheck(checkConfiguration.getId(), checkConfiguration);
                     break;
                default:
                    LOG.error("Unknown check type [{}]. Skipping.", checkConfiguration.getType());
                    continue;
            }

            if (check.disabled()) {
                disabledChecks.add(check);
                continue;
            }

            try {
                if (cliArguments.hasTags() && checkConfiguration.hasARequestedTag(cliArguments.getTags())) {
                    check.run();
                    issues.addAll(check.getIssues());
                } else {
                    LOG.info("Not running check [{}] because it does not have any of the requested tags.", checkConfiguration.getId());
                }
            } catch(FatalCheckException e) {
                LOG.error("Fatal error in check [{}]. Aborting.", check.getFullCheckIdentifier(), e);
            }
        }

        ImmutableList<Issue> finalIssues = issues.build();
        if(finalIssues.isEmpty()) {
            LOG.info("No issues detected. (•̀ᴗ•́)و̑̑");
        } else {
            for (Issue issue : finalIssues) {
                LOG.warn("Check {}: {}", issue.getCheck().getFullCheckIdentifier(), issue.getMessage());
            }
        }

        for (Check disabledCheck : disabledChecks.build()) {
            LOG.warn("Check [{}] is disabled and was not executed.", disabledCheck.getFullCheckIdentifier());
        }

    }

}
