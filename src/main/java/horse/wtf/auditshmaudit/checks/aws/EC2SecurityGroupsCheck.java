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

package horse.wtf.auditshmaudit.checks.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;
import com.google.common.base.Strings;
import horse.wtf.auditshmaudit.configuration.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class EC2SecurityGroupsCheck {

    /*private static final Logger LOG = LogManager.getLogger(EC2SecurityGroupsCheck.class);

    private static final String CIDR_ALL_IPV4 = "0.0.0.0/0";
    private static final String CIDR_ALL_IPV6 = "::/0";

    private final Configuration configuration;

    @Inject
    public EC2SecurityGroupsCheck(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected List<Issue> check() {
        for (Regions region : Regions.values()) {
            try {
                runForRegion(region);
            } catch(Exception e) {
                // lol exception types
                if(e.getMessage().contains("Status Code: 401")) {
                    LOG.warn("User not allowed to run in region [{}]. Skipping.", region.getLongCheckName());
                } else {
                    LOG.error(e);
                }
            }
        }

        return issues();
    }

    private void runForRegion(Regions region) {
        AmazonEC2 client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                configuration.getCheckAWSSecurityGroupsAccessKeyId(),
                                configuration.getCheckAWSSecurityGroupsAccessKeySecret())
                ))
                .withRegion(region)
                .build();

        DescribeSecurityGroupsResult result = client.describeSecurityGroups(new DescribeSecurityGroupsRequest().withMaxResults(1000));
        if(result.getSecurityGroups().size() == 1000) {
            LOG.warn("Only running against a maximum of 1000 security groups.");
        }

        for (SecurityGroup group : result.getSecurityGroups()) {
            checkRules(group, group.getIpPermissions(), configuration.getCheckAWSSecurityGroupsCriticalPortsInbound(), "inbound");
            checkRules(group, group.getIpPermissionsEgress(), configuration.getCheckAWSSecurityGroupsCriticalPortsOutbound(), "outbound");
        }
    }

    private void checkRules(SecurityGroup group, List<IpPermission> permissions, List<Configuration.PortAndProtocol> criticalPorts, String direction) {
        for (IpPermission permission : permissions) {
            for (Configuration.PortAndProtocol critical : criticalPorts) {
                if ((critical.getProtocol().equals(permission.getIpProtocol()) || permission.getIpProtocol() == null)
                        && ((critical.getPort() >= permission.getFromPort() && critical.getPort() <= permission.getToPort())) || isAllRange(permission)) {
                    // This rule is in range of a critical port.

                    // Check if it's open to the IPv4 world.
                    for (IpRange range : permission.getIpv4Ranges()) {
                        if (CIDR_ALL_IPV4.equals(range.getCidrIp())) {
                            addIssue(new Issue(this.getClass(), "Security group [{}] has critical port <{}> open to [{}] {}.",
                                    group.getGroupName(), critical, range.getCidrIp(), direction));
                            break;
                        }

                    }

                    // Check if it's open to the IPv6 world.
                    for (Ipv6Range range : permission.getIpv6Ranges()) {
                        if (CIDR_ALL_IPV6.equals(range.getCidrIpv6())) {
                            addIssue(new Issue(this.getClass(), "Security group [{}] has critical port <{}> open to [{}] {}.",
                                    group.getGroupName(), critical, range.getCidrIpv6(), direction));
                            break;
                        }
                    }
                }
            }
        }
    }

    // meh amazon
    private boolean isAllRange(IpPermission permission) {
        return permission.getFromPort() == null && permission.getToPort() == null;
    }

    @Override
    public boolean disabled() {
        return !configuration.isCheckAWSSecurityGroupsEnabled();
    }

    @Override
    public boolean configurationComplete() {
        return !Strings.isNullOrEmpty(configuration.getCheckAWSSecurityGroupsAccessKeyId())
                && !Strings.isNullOrEmpty(configuration.getCheckAWSSecurityGroupsAccessKeySecret());
    }*/

}
