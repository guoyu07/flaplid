/*
 *  This file is part of flaplid.
 *
 *  flaplid is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  flaplid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with flaplid.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.flaplid.checks.graylog;

import com.fasterxml.jackson.databind.ObjectMapper;
import horse.wtf.flaplid.Issue;
import horse.wtf.flaplid.checks.Check;
import horse.wtf.flaplid.checks.graylog.models.User;
import horse.wtf.flaplid.checks.graylog.models.UsersList;
import horse.wtf.flaplid.configuration.CheckConfiguration;
import okhttp3.*;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Arrays;

public class GraylogUsersCheck extends Check {

    public static final String TYPE = "graylog_users";

    private static final String C_GRAYLOG_API_URL = "graylog_api_url";
    private static final String C_USERNAME = "username";
    private static final String C_PASSWORD = "password";
    private static final String C_MAX_USER_INACTIVITY_DAYS = "maximum_user_inactivity_days";

    public final CheckConfiguration configuration;
    private final OkHttpClient httpClient;
    private final ObjectMapper om;

    private HttpUrl url;

    public GraylogUsersCheck(String id, CheckConfiguration configuration, OkHttpClient httpClient, ObjectMapper om) {
        super(id, configuration);

        this.configuration = configuration;
        this.httpClient = httpClient;
        this.om = om;

        this.url = HttpUrl.parse(configuration.getString(C_GRAYLOG_API_URL)).newBuilder()
                .addPathSegment("users")
                .build();
    }

    @Override
    protected void check() {
        try {
            Response result = this.httpClient.newCall(
                    new Request.Builder()
                            .header("Authorization", Credentials.basic(
                                    configuration.getString(C_USERNAME),
                                    configuration.getString(C_PASSWORD)
                                    )
                            ).url(this.url).build()
            ).execute();

            if(result.code() != 200) {
                throw new RuntimeException("Unexpected Graylog HTTP response code [" + result.code() + "]");
            }

            if(result.body() == null) {
                throw new RuntimeException("Empty response from Graylog.");
            }

            UsersList users = this.om.readValue(result.body().string(), UsersList.class);

            for (User user : users.users) {
                if (user.lastActivity == null) {
                    addIssue(new Issue(this, "Graylog user has never logged in: {} <{}> ({})", user.username, user.email, user.fullName));
                }

                DateTime lastActivity = new DateTime(user.lastActivity);
                if(lastActivity.isBefore(DateTime.now().minusDays(configuration.getInt(C_MAX_USER_INACTIVITY_DAYS)))) {
                    addIssue(new Issue(this, "Graylog user with no login in last <{}> days: {} <{}> ({}). Last login: {}",
                            configuration.getInt(C_MAX_USER_INACTIVITY_DAYS),
                            user.username, user.email, user.fullName,
                            lastActivity));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error when trying to communicate with Graylog API.", e);
        }
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean isConfigurationComplete() {
        return configuration.isComplete(Arrays.asList(
                C_GRAYLOG_API_URL,
                C_USERNAME,
                C_PASSWORD
        ));
    }

}

