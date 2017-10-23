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

import com.github.joschi.jadconfig.Parameter;
import com.google.inject.Singleton;

@Singleton
public class Configuration {

    // Mandatory check flags.
    @Parameter(value = "check_aws_iam_enabled", required = true)
    private boolean checkAWSIAMEnabled;

    @Parameter(value = "check_github_org_enabled", required = true)
    private boolean checkGitHubOrganizationEnabled;

    @Parameter(value = "check_slack_team_enabled", required = true)
    private boolean checkSlackTeamEnabled;

    // AWS: IAM
    @Parameter(value = "check_aws_iam_access_key_id")
    private String checkAWSIAMAccessKeyId;

    @Parameter(value = "check_aws_iam_access_key_secret")
    private String checkAWSIAMAccessKeySecret;

    @Parameter(value = "check_aws_iam_maximum_user_inactivity_days")
    private int checkAWSIAMMaximumUserInactivityDays;

    @Parameter(value = "check_aws_iam_maximum_access_key_inactivity_days")
    private int checkAWSIAMMaximumAccessKeyInactivityDays;

    @Parameter(value = "check_aws_iam_minimum_password_length")
    private int checkAWSIAMMinimumPasswordLength;

    @Parameter(value = "check_aws_iam_maximum_password_age")
    private int checkAWSIAMMaximumPasswordAge;

    // GitHub: Organization
    @Parameter(value = "check_github_org_access_key")
    private String checkGitHubOrganizationAccessKey;

    @Parameter(value = "check_github_org_username")
    private String checkGitHubOrganizationUsername;

    @Parameter(value = "check_github_org_organization_name")
    private String checkGitHubOrganizationOrganizationName;

    // Slack: Team
    @Parameter(value = "check_slack_team_oauth_token")
    private String checkSlackTeamOauthToken;

    public boolean isCheckAWSIAMEnabled() {
        return checkAWSIAMEnabled;
    }

    public boolean isCheckGitHubOrganizationEnabled() {
        return checkGitHubOrganizationEnabled;
    }

    public boolean isCheckSlackTeamEnabled() {
        return checkSlackTeamEnabled;
    }

    public String getCheckAWSIAMAccessKeyId() {
        return checkAWSIAMAccessKeyId;
    }

    public String getCheckAWSIAMAccessKeySecret() {
        return checkAWSIAMAccessKeySecret;
    }

    public int getCheckAWSIAMMaximumUserInactivityDays() {
        return checkAWSIAMMaximumUserInactivityDays;
    }

    public int getCheckAWSIAMMaximumAccessKeyInactivityDays() {
        return checkAWSIAMMaximumAccessKeyInactivityDays;
    }

    public int getCheckAWSIAMMinimumPasswordLength() {
        return checkAWSIAMMinimumPasswordLength;
    }

    public int getCheckAWSIAMMaximumPasswordAge() {
        return checkAWSIAMMaximumPasswordAge;
    }

    public String getCheckGitHubOrganizationAccessKey() {
        return checkGitHubOrganizationAccessKey;
    }

    public String getCheckGitHubOrganizationUsername() {
        return checkGitHubOrganizationUsername;
    }

    public String getCheckGitHubOrganizationOrganizationName() {
        return checkGitHubOrganizationOrganizationName;
    }

    public String getCheckSlackTeamOauthToken() {
        return checkSlackTeamOauthToken;
    }

}
