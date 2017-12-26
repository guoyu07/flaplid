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

import horse.wtf.auditshmaudit.configuration.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GitHubOrganizationCheck extends Check {

    public static final String TYPE = "github_organization";

    private static final String C_ORGANIZATION_NAME = "organization_name";
    private static final String C_USERNAME = "username";
    private static final String C_ACCESS_KEY = "access_key";

    private final Configuration configuration;

    public GitHubOrganizationCheck(String checkId, Configuration configuration) {
        super(checkId, configuration);

        this.configuration = configuration;
    }

    @Override
    protected void check() {
        try {
            GitHub client = GitHub.connect(
                    configuration.getString(this, C_USERNAME),
                    configuration.getString(this, C_ACCESS_KEY)
            );
            GHOrganization organization = client.getOrganization(
                    configuration.getString(this, C_ORGANIZATION_NAME)
            );

            PagedIterable<GHUser> usersWithoutMFA = organization.listMembersWithFilter("2fa_disabled");
            for (GHUser user : usersWithoutMFA) {
                addIssue(new Issue(this, "User without enabled MFA: {}", user.getLogin()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when trying to communicate with GitHub API.", e);
        }
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean isConfigurationComplete() {
        return configuration.isCheckConfigurationComplete(this, Arrays.asList(
                C_ORGANIZATION_NAME,
                C_USERNAME,
                C_ACCESS_KEY
        ));
    }
}
