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
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

@Singleton
public class Configuration {

    private static final Logger LOG = LogManager.getLogger(Configuration.class);

    // Mandatory check flags.
    @Parameter(value = "check_aws_iam_enabled", required = true)
    private boolean checkAWSIAMEnabled;

    @Parameter(value = "check_github_org_enabled", required = true)
    private boolean checkGitHubOrganizationEnabled;

    @Parameter(value = "check_slack_team_enabled", required = true)
    private boolean checkSlackTeamEnabled;

    @Parameter(value = "check_aws_security_groups_enabled", required = true)
    private boolean checkAWSSecurityGroupsEnabled;

    @Parameter(value = "check_website_downloads_enabled", required = true)
    private boolean checkWebsiteDownloadsEnabled;

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

    // AWS: Security Groups
    @Parameter(value = "check_aws_security_groups_access_key_id")
    private String checkAWSSecurityGroupsAccessKeyId;

    @Parameter(value = "check_aws_security_groups_access_key_secret")
    private String checkAWSSecurityGroupsAccessKeySecret;

    @Parameter(value = "check_aws_security_groups_critical_ports_inbound")
    private String checkAWSSecurityGroupsCriticalPortsInbound;

    @Parameter(value = "check_aws_security_groups_critical_ports_outbound")
    private String checkAWSSecurityGroupsCriticalPortsOutbound;

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

    public boolean isCheckAWSSecurityGroupsEnabled() {
        return checkAWSSecurityGroupsEnabled;
    }

    public boolean isCheckWebsiteDownloadsEnabled() {
        return checkWebsiteDownloadsEnabled;
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

    public String getCheckAWSSecurityGroupsAccessKeyId() {
        return checkAWSSecurityGroupsAccessKeyId;
    }

    public String getCheckAWSSecurityGroupsAccessKeySecret() {
        return checkAWSSecurityGroupsAccessKeySecret;
    }

    public List<PortAndProtocol> getCheckAWSSecurityGroupsCriticalPortsInbound() {
        return parseListOfPorts(checkAWSSecurityGroupsCriticalPortsInbound);
    }

    public List<PortAndProtocol> getCheckAWSSecurityGroupsCriticalPortsOutbound() {
        return parseListOfPorts(checkAWSSecurityGroupsCriticalPortsOutbound);
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

    private List<PortAndProtocol> parseListOfPorts(String s) {
        if(s == null || Strings.isNullOrEmpty(s)) {
            return Collections.emptyList();
        }

        ImmutableList.Builder<PortAndProtocol> ports = new ImmutableList.Builder<>();
        for (String x : Splitter.on(",").omitEmptyStrings().trimResults().split(s)) {
            try {
                if(!x.contains("/")) {
                    throw new RuntimeException("Malformed port entry.");
                }

                String[] parts = x.split("/");
                ports.add(new PortAndProtocol(parts[0], Integer.parseInt(parts[1])));
            } catch(Exception e) {
                LOG.error("Could not parse critical port. Skipping.", e);
                continue;
            }
        }

        return ports.build();
    }

    public class PortAndProtocol {

        private final String protocol;
        private final int port;

        public PortAndProtocol(String protocol, int port) {
            this.protocol = protocol;
            this.port = port;
        }

        public String getProtocol() {
            return protocol;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return protocol + "/" + port;
        }

    }
}
