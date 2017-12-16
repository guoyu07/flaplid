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
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.*;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import horse.wtf.auditshmaudit.configuration.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.checks.aws.convenience.AWSAccessKey;
import horse.wtf.auditshmaudit.checks.aws.convenience.AWSUser;
import org.joda.time.DateTime;

import java.util.List;

public class AWSIAMCheck {

    /*    private final Configuration configuration;

    @Inject
    public AWSIAMCheck(Configuration configuration) {
        this.configuration = configuration;
    }

    protected List<Issue> check() {
        AmazonIdentityManagement client = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                                configuration.getCheckAWSIAMAccessKeyId(),
                                configuration.getCheckAWSIAMAccessKeySecret())
                        ))
                .withRegion(Regions.DEFAULT_REGION)
                .build();

        // Get users.
        List<AWSUser> users = Lists.newArrayList();
        ListUsersResult luResult = client.listUsers();
        if(luResult.isTruncated()) { throw new RuntimeException("Users result is truncated!"); }
        for (User userInfo : luResult.getUsers()) {
            // Get access keys of this user.
            ListAccessKeysRequest keysRequest = new ListAccessKeysRequest();
            keysRequest.setUserName(userInfo.getUserName());
            ListAccessKeysResult accessKeys = client.listAccessKeys(keysRequest);
            if(accessKeys.isTruncated()) {
                throw new RuntimeException("Access Keys result of user [" + userInfo.getUserName() + "] is truncated!");
            }

            // For each access key, get its last used date and build a helper object.
            List<AWSAccessKey> userAccessKeys = Lists.newArrayList();
            for (AccessKeyMetadata keyInfo : accessKeys.getAccessKeyMetadata()) {
                // Get last used date.
                GetAccessKeyLastUsedResult accessKeyLastUsed = client.getAccessKeyLastUsed(
                        new GetAccessKeyLastUsedRequest().withAccessKeyId(keyInfo.getAccessKeyId())
                );

                userAccessKeys.add(new AWSAccessKey(
                        AWSAccessKey.STATUS.valueOf(keyInfo.getStatus().toUpperCase()),
                        new DateTime(keyInfo.getCreateDate()),
                        new DateTime(accessKeyLastUsed.getAccessKeyLastUsed().getLastUsedDate())
                ));
            }

            // Get all virtual and physical MFA devices and count them.
            int mfaCount = 0;
            ListMFADevicesResult mfaResult = client.listMFADevices(new ListMFADevicesRequest(userInfo.getUserName()));
            if(mfaResult.isTruncated()) {
                throw new RuntimeException("MFA result of user [" + userInfo.getUserName() + "] is truncated!");
            }

            mfaCount += mfaResult.getMFADevices().size();

            users.add(new AWSUser(
                    userInfo.getUserName(),
                    userInfo.getPasswordLastUsed() == null ? null : new DateTime(userInfo.getPasswordLastUsed()),
                    new DateTime(userInfo.getCreateDate()),
                    userAccessKeys,
                    mfaCount
            ));
        }

        // Run general user checks.
        for (AWSUser user : users) {
            // User not in use?
            if (user.getPasswordLastUsed() != null
                    && user.getPasswordLastUsed().isBefore(DateTime.now().minusDays(configuration.getCheckAWSIAMMaximumUserInactivityDays()))) {
                addIssue(new Issue(this.getClass(), "User with no login in last <{}> days: {}. Last login: {}",
                        configuration.getCheckAWSIAMMaximumUserInactivityDays(), user.getUsername(), user.getPasswordLastUsed()));
            }

            // Access key not in use?
            for (AWSAccessKey accessKey : user.getAccessKeys()) {
                if(accessKey.getStatus().equals(AWSAccessKey.STATUS.ACTIVE)
                        && accessKey.getLastUsed().isBefore(DateTime.now().minusDays(configuration.getCheckAWSIAMMaximumAccessKeyInactivityDays()))) {
                    addIssue(new Issue(this.getClass(), "An active access key for user [{}] has not been used in last <{}> days. Last used: {}, created at: {}",
                            user.getUsername(), configuration.getCheckAWSIAMMaximumAccessKeyInactivityDays(), accessKey.getLastUsed(), accessKey.getCreateDate()));
                }
            }

            // 2FA enabled if this is a console login user?
            if(user.getPasswordLastUsed() != null && user.getMfaDeviceCount() == 0) {
                addIssue(new Issue(this.getClass(), "User has logged in to console in the past but no MFA device configured: {}", user.getUsername()));
            }
        }

        // Do we have a proper password policy set?
        PasswordPolicy passwordPolicy = client.getAccountPasswordPolicy().getPasswordPolicy();
        if(passwordPolicy.getMinimumPasswordLength() < configuration.getCheckAWSIAMMinimumPasswordLength()) {
            addIssue(new Issue(this.getClass(), "There is no password policy that enforces passwords with at least <{}> characters.",
                    configuration.getCheckAWSIAMMinimumPasswordLength()));
        }

        if(passwordPolicy.getMaxPasswordAge() == null
                || passwordPolicy.getMaxPasswordAge() <= 0
                || passwordPolicy.getMaxPasswordAge() < configuration.getCheckAWSIAMMaximumPasswordAge()) {
            addIssue(new Issue(this.getClass(), "There is no password policy that enforces password change after <{}> days.",
                    configuration.getCheckAWSIAMMaximumPasswordAge()));
        }

        if(passwordPolicy.getPasswordReusePrevention() == null || passwordPolicy.getPasswordReusePrevention() <= 0) {
            addIssue(new Issue(this.getClass(), "Password policy allows password reuse."));
        }

        return issues();
    }

    @Override
    public boolean disabled() {
        return !configuration.isCheckAWSIAMEnabled();
    }

    @Override
    public boolean configurationComplete() {
        return !Strings.isNullOrEmpty(configuration.getCheckAWSIAMAccessKeyId())
                && !Strings.isNullOrEmpty(configuration.getCheckAWSIAMAccessKeySecret())
                && configuration.getCheckAWSIAMMaximumUserInactivityDays() > 0
                && configuration.getCheckAWSIAMMaximumAccessKeyInactivityDays() > 0
                && configuration.getCheckAWSIAMMinimumPasswordLength() > 0
                && configuration.getCheckAWSIAMMaximumPasswordAge() > 0;
    }*/

}
