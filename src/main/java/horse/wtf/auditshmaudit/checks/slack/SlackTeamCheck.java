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
import com.google.inject.Inject;
import horse.wtf.auditshmaudit.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.checks.slack.models.SlackMember;
import horse.wtf.auditshmaudit.checks.slack.models.SlackUsersList;
import okhttp3.*;

import java.io.IOException;
import java.util.List;

public class SlackTeamCheck extends Check {

    private static final String NAME = "Slack: Team Members";

    private final Configuration configuration;
    private final OkHttpClient httpClient;
    private final ObjectMapper om;

    @Inject
    public SlackTeamCheck(Configuration configuration, OkHttpClient httpClient, ObjectMapper om) {
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.om = om;
    }

    @Override
    protected List<Issue> check() {
        // Find user with no enabled MFA.
        try {
            Response result = this.httpClient.newCall(new Request.Builder()
                    .url(new HttpUrl.Builder()
                            .scheme("https")
                            .host("slack.com")
                            .encodedPath("/api/users.list")
                            .addQueryParameter("token", configuration.getCheckSlackTeamOauthToken())
                            .addQueryParameter("include_locale", "false")
                            .addQueryParameter("limit", "0")
                            .addQueryParameter("presence", "false")
                            .build()
                    )
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
                    addIssue(new Issue(this.getClass(), "Team member has no MFA device configured: {} ({})", member.name, member.realName));
                }
            }


        } catch (IOException e) {
            throw new RuntimeException("Error when trying to communicate with Slack.", e);
        }

        return issues();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean disabled() {
        return !configuration.isCheckSlackTeamEnabled();
    }

    @Override
    public boolean configurationComplete() {
        return true;
    }

}