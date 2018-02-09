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

package horse.wtf.flaplid.checks;

import com.google.common.collect.ImmutableList;
import horse.wtf.flaplid.Issue;
import horse.wtf.flaplid.attic.Attic;
import horse.wtf.flaplid.configuration.CheckConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Check {

    private static final Logger LOG = LogManager.getLogger(Check.class);

    private final ImmutableList.Builder<Issue> issuesBuilder;

    private final String id;
    private final CheckConfiguration configuration;
    private Attic attic;

    protected abstract void check() throws FatalCheckException;
    public abstract String getCheckType();
    public abstract boolean isConfigurationComplete();

    protected Check(String id, CheckConfiguration configuration) {
        this.id = id;
        this.issuesBuilder = new ImmutableList.Builder<>();
        this.configuration = configuration;
    }

    public void run() throws FatalCheckException {
        this.attic = new Attic(this, configuration.getAtticBaseFolderPath());

        LOG.info("Running check [{}].", getFullCheckIdentifier());

        if(!disabled() && !isConfigurationComplete()) {
            throw new FatalCheckException("Missing configuration parameters for check [" + getFullCheckIdentifier() + "] and check is not disabled.");
        }

        check();
    }

    public boolean disabled() {
        return !configuration.isEnabled();
    }

    public String getCheckId() {
        return id;
    }

    public String getFullCheckIdentifier() {
        return getCheckType() + ":" + getCheckId() + "#" + configuration.getSeverity().toString().toLowerCase();
    }

    protected Attic getAttic() {
        return attic;
    }

    protected void addIssue(Issue issue) {
        this.issuesBuilder.add(issue);
    }

    public ImmutableList<Issue> getIssues() {
        return this.issuesBuilder.build();
    }

}
