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

package horse.wtf.auditshmaudit.checks.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import horse.wtf.auditshmaudit.configuration.CheckConfiguration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.checks.slack.models.SlackMember;
import horse.wtf.auditshmaudit.checks.slack.models.SlackUsersList;
import okhttp3.*;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.util.Arrays;

public class SlackTeamCheck extends Check {

    public static final String TYPE = "slack_team";

    private final String C_OAUTH_TOKEN = "oauth_token";

    private final CheckConfiguration configuration;
    private final OkHttpClient httpClient;
    private final ObjectMapper om;

    private HttpUrl url;

    public SlackTeamCheck(String checkId, CheckConfiguration configuration, OkHttpClient httpClient, ObjectMapper om) {
        super(checkId, configuration);
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.om = om;

        this.url = new HttpUrl.Builder()
                .scheme("https")
                .host("slack.com")
                .encodedPath("/api/users.list")
                .addQueryParameter("token", configuration.getString(C_OAUTH_TOKEN))
                .addQueryParameter("include_locale", "false")
                .addQueryParameter("limit", "0")
                .addQueryParameter("presence", "false")
                .build();
    }

    @Override
    protected void check() {
        // Find user with no enabled MFA.
        try {
            Response result = this.httpClient.newCall(new Request.Builder()
                    .url(url)
                    .build()
            ).execute();

            if(result.code() != 200) {
                throw new RuntimeException("Unexpected Slack HTTP response code [" + result.code() + "]");
            }

            if(result.body() == null) {
                throw new RuntimeException("Empty response from Slack.");
            }

            SlackUsersList users = this.om.readValue(result.body().string(), SlackUsersList.class);

            // lol REST
            if(!users.ok) {
                if(Strings.isNullOrEmpty(users.error)) {
                    throw new RuntimeException("Slack reported an error but no error message was provided.");
                } else {
                    if(users.error.equals("missing_scope")) {
                        throw new RuntimeException("Slack user is missing permissions on scope [" + users.needed + "].");
                    } else {
                        throw new RuntimeException("Slack reported an error: " + users.error);
                    }
                }
            }

            for (SlackMember member : users.members) {
                if (member.isBot || member.deleted) {
                    continue;
                }

                if(!member.has2FA) {
                    addIssue(new Issue(this, "Team member has no MFA device configured: {} ({})", member.name, member.realName));
                }
            }


        } catch (IOException e) {
            throw new RuntimeException("Error when trying to communicate with Slack.", e);
        }
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean isConfigurationComplete() {
        return configuration.isComplete(Arrays.asList(
                C_OAUTH_TOKEN
        ));
    }

    /**
     * For testing.
     *
     * @param url The Slack API URL
     */
    public void setUrl(HttpUrl url) {
        this.url = url;
    }



}
