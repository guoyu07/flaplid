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

package horse.wtf.auditshmaudit.checks.github;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import horse.wtf.auditshmaudit.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.util.List;

public class GitHubOrganizationCheck extends Check {

    private static final String NAME = "GitHub: Organization Members";

    private final Configuration configuration;

    @Inject
    public GitHubOrganizationCheck(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected List<Issue> check() {
        try {
            GitHub client = GitHub.connect(
                    configuration.getCheckGitHubOrganizationUsername(),
                    configuration.getCheckGitHubOrganizationAccessKey()
            );
            GHOrganization organization = client.getOrganization(
                    configuration.getCheckGitHubOrganizationOrganizationName()
            );

            PagedIterable<GHUser> usersWithoutMFA = organization.listMembersWithFilter("2fa_disabled");
            for (GHUser user : usersWithoutMFA) {
                addIssue(new Issue(this.getClass(), "User without enabled MFA: {}", user.getLogin()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when trying to communicate with GitHub API.", e);
        }

        return issues();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean disabled() {
        return !configuration.isCheckGitHubOrganizationEnabled();
    }

    @Override
    public boolean configurationComplete() {
        return !Strings.isNullOrEmpty(configuration.getCheckGitHubOrganizationUsername())
                && !Strings.isNullOrEmpty(configuration.getCheckGitHubOrganizationAccessKey())
                && !Strings.isNullOrEmpty(configuration.getCheckGitHubOrganizationOrganizationName());
    }

}
