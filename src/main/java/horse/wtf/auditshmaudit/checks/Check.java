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

package horse.wtf.auditshmaudit.checks;

import com.google.common.collect.ImmutableList;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.attic.Attic;
import horse.wtf.auditshmaudit.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public abstract class Check {

    private static final Logger LOG = LogManager.getLogger(Check.class);

    private final ImmutableList.Builder<Issue> issuesBuilder;

    private final String id;
    private final Configuration configuration;
    private Attic attic;

    protected abstract List<Issue> check();
    public abstract String getCheckType();
    public abstract boolean disabled();
    public abstract boolean configurationComplete();

    protected Check(String id, Configuration configuration) {
        this.id = id;
        this.issuesBuilder = new ImmutableList.Builder<>();
        this.configuration = configuration;
    }

    public List<Issue> run() throws FatalCheckException {
        this.attic = new Attic(this, configuration.atticFolder);

        LOG.info("Running check [{}].", getFullCheckIdentifier());

        if(!disabled() && !configurationComplete()) {
            throw new FatalCheckException("Missing configuration parameters for check [" + getFullCheckIdentifier() + "] and check is not disabled.");
        }

        try {
            return check();
        } catch(Exception e){
            throw new FatalCheckException(e);
        }

    }

    public String getCheckId() {
        return id;
    }

    public String getFullCheckIdentifier() {
        return getCheckType() + ":" + getCheckId();
    }

    protected Attic getAttic() {
        return attic;
    }

    protected void addIssue(Issue issue) {
        this.issuesBuilder.add(issue);
    }

    protected ImmutableList<Issue> issues() {
        return this.issuesBuilder.build();
    }

}
