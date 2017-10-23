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
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.PropertiesRepository;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.checks.CheckModule;
import horse.wtf.auditshmaudit.checks.FatalCheckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    private static final int FAILURE = 1;

    public static void main(String[] argv) {
        LOG.info("Starting up.");

        final CLIArguments cliArguments = new CLIArguments();
        final Configuration configuration = new Configuration();

        // Parse CLI arguments.
        JCommander.newBuilder()
                .addObject(cliArguments)
                .build()
                .parse(argv);

        // Parse configuration.
        try {
            new JadConfig(new PropertiesRepository(cliArguments.getConfigFilePath()), configuration).process();
        } catch (RepositoryException | ValidationException e) {
            LOG.error("Could not read config.", e);
            Runtime.getRuntime().exit(FAILURE);
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
