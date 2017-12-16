/*
 *  This file is part of auditshmaudit.
 *
 *  auditshmaudit is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  auditshmaudit is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with auditshmaudit.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.auditshmaudit;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.checks.CheckModule;
import horse.wtf.auditshmaudit.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    private static final int FAILURE = 1;

    public static void main(String[] argv) {
        LOG.info("Starting up.");

        final CLIArguments cliArguments = new CLIArguments();

        // Parse CLI arguments.
        JCommander.newBuilder()
                .addObject(cliArguments)
                .build()
                .parse(argv);

        // Parse configuration.
        Configuration configuration = null;
        try {
            File file = new File(cliArguments.getConfigFilePath());
            CharSource source = Files.asCharSource(file, Charsets.UTF_8);

            ObjectMapper configObjectMapper = new ObjectMapper(new YAMLFactory());
            configuration = configObjectMapper.readValue(source.read(), Configuration.class);
        } catch (Exception e) {
            LOG.info("Could not read configuration file.", e);
            System.exit(FAILURE);
        }

        if (!configuration.isComplete()) {
            LOG.info("Configuration is incomplete. Please refer to the example configuration file.");
            System.exit(FAILURE);
        }

        Injector injector = Guice.createInjector(new CheckModule(configuration));

        ImmutableList.Builder<Check> disabledChecks = new ImmutableList.Builder<>();
        ImmutableList.Builder<Issue> issues = new ImmutableList.Builder<>();

        Reflections reflections = new Reflections("horse.wtf.auditshmaudit.checks");
        for (Class<? extends Check> c : reflections.getSubTypesOf(Check.class)) {
            try {
                Check check = injector.getInstance(c);

                if(check.disabled()) {
                    disabledChecks.add(check);
                } else {
                    issues.addAll(check.run());
                }
            } catch(Exception e) {
                LOG.error("Fatal error in check [{}]. Aborting.", c.getCanonicalName(), e);
                System.exit(FAILURE);
            }
        }

        for (Issue issue : issues.build()) {
            LOG.info("Check {}: {}", issue.getCheckName(), issue.getMessage());
        }

        for (Check disabledCheck : disabledChecks.build()) {
            LOG.warn("Check [{}] is disabled and was not executed.", disabledCheck.getName());
        }

        // TODO: Run OpsGenie heartbeat if in --prod mode. (optional)

    }

}
