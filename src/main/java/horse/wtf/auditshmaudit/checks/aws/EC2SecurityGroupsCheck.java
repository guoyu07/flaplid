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
import horse.wtf.auditshmaudit.configuration.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class EC2SecurityGroupsCheck extends Check {

    private static final Logger LOG = LogManager.getLogger(EC2SecurityGroupsCheck.class);

    public static final String TYPE = "aws_security_groups";

    private static final String C_ACCESS_KEY = "access_key";
    private static final String C_ACCESS_KEY_SECRET = "access_key_secret";
    private static final String C_CRITICAL_PORTS_INBOUND = "critical_ports_inbound";
    private static final String C_CRITICAL_PORTS_OUTBOUND = "critical_ports_outbound";

    private static final String CIDR_ALL_IPV4 = "0.0.0.0/0";
    private static final String CIDR_ALL_IPV6 = "::/0";

    private final Configuration configuration;

    public EC2SecurityGroupsCheck(String checkId, Configuration configuration) {
        super(checkId, configuration);
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
                    LOG.warn("User not allowed to run in region [{}]. Skipping.", region.getName());
                } else {
                    LOG.error(e);
                }
            }
        }

        return issues();
    }

    private void runForRegion(Regions region) {
        LOG.info("Running for region [{}].", region.getName());
        AmazonEC2 client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                configuration.getString(this, C_ACCESS_KEY),
                                configuration.getString(this, C_ACCESS_KEY_SECRET))
                ))
                .withRegion(region)
                .build();

        DescribeSecurityGroupsResult result = client.describeSecurityGroups(new DescribeSecurityGroupsRequest().withMaxResults(1000));
        if(result.getSecurityGroups().size() == 1000) {
            LOG.warn("Only running against a maximum of 1000 security groups.");
        }

        for (SecurityGroup group : result.getSecurityGroups()) {
            checkRules(group, group.getIpPermissions(), configuration.getListOfPortsAndProtocols(this, C_CRITICAL_PORTS_INBOUND), "inbound", region);
            checkRules(group, group.getIpPermissionsEgress(), configuration.getListOfPortsAndProtocols(this, C_CRITICAL_PORTS_OUTBOUND), "outbound", region);
        }
    }

    private void checkRules(SecurityGroup group, List<IpPermission> permissions, List<Configuration.PortAndProtocol> criticalPorts, String direction, Regions region) {
        for (IpPermission permission : permissions) {
            for (Configuration.PortAndProtocol critical : criticalPorts) {
                if ((critical.getProtocol().equals(permission.getIpProtocol()) || permission.getIpProtocol() == null)
                        && ((critical.getPort() >= permission.getFromPort() && critical.getPort() <= permission.getToPort())) || isAllRange(permission)) {
                    // This rule is in range of a critical port.

                    // Check if it's open to the IPv4 world.
                    for (IpRange range : permission.getIpv4Ranges()) {
                        if (CIDR_ALL_IPV4.equals(range.getCidrIp())) {
                            addIssue(new Issue(this, "[{}] Security group [{}] has critical port <{}> open to [{}] {}.",
                                    region.getName(), group.getGroupName(), critical, range.getCidrIp(), direction));
                            break;
                        }

                    }

                    // Check if it's open to the IPv6 world.
                    for (Ipv6Range range : permission.getIpv6Ranges()) {
                        if (CIDR_ALL_IPV6.equals(range.getCidrIpv6())) {
                            addIssue(new Issue(this, "[{}] Security group [{}] has critical port <{}> open to [{}] {}.",
                                    region.getName(), group.getGroupName(), critical, range.getCidrIpv6(), direction));
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
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean configurationComplete() {
        return configuration.isCheckConfigurationComplete(this, Arrays.asList(
                C_ACCESS_KEY,
                C_ACCESS_KEY_SECRET,
                C_CRITICAL_PORTS_INBOUND,
                C_CRITICAL_PORTS_OUTBOUND
        ));
    }

}
