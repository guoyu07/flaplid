package horse.wtf.flaplid.checks.slack;

import horse.wtf.flaplid.checks.CheckTestHelper;
import horse.wtf.flaplid.configuration.CheckConfiguration;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.testng.annotations.Test;
import org.testng.collections.Maps;

import java.io.IOException;

import static org.testng.Assert.*;

public class SlackTeamCheckTest extends CheckTestHelper {

    public static final String RESPONSE_ISSUEFREE = "{\n" +
            "  \"ok\": true,\n" +
            "  \"members\": [\n" +
            "    {\n" +
            "      \"id\": \"A1BCDEFGH\",\n" +
            "      \"team_id\": \"A2BCDEFGH\",\n" +
            "      \"name\": \"a-bot\",\n" +
            "      \"deleted\": false,\n" +
            "      \"color\": \"827327\",\n" +
            "      \"real_name\": \"a-bot\",\n" +
            "      \"tz\": \"America/Los_Angeles\",\n" +
            "      \"tz_label\": \"Pacific Standard Time\",\n" +
            "      \"tz_offset\": -28800,\n" +
            "      \"profile\": {\n" +
            "        \"bot_id\": \"A1BCDEFGH\",\n" +
            "        \"api_app_id\": \"A1BCDEFGH\",\n" +
            "        \"always_active\": false,\n" +
            "        \"real_name\": \"a-bot\",\n" +
            "        \"display_name\": \"\",\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"real_name_normalized\": \"abot\",\n" +
            "        \"display_name_normalized\": \"\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_admin\": false,\n" +
            "      \"is_owner\": false,\n" +
            "      \"is_primary_owner\": false,\n" +
            "      \"is_restricted\": false,\n" +
            "      \"is_ultra_restricted\": false,\n" +
            "      \"is_bot\": true,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"presence\": \"away\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"B1BCDEFGH\",\n" +
            "      \"team_id\": \"B2BCDEFGH\",\n" +
            "      \"name\": \"A deleted user\",\n" +
            "      \"deleted\": true,\n" +
            "      \"profile\": {\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"real_name\": \"a-deleted-user\",\n" +
            "        \"display_name\": \"\",\n" +
            "        \"real_name_normalized\": \"chris\",\n" +
            "        \"display_name_normalized\": \"\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_bot\": false,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"presence\": \"away\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"C1BCDEFGH\",\n" +
            "      \"team_id\": \"C2BCDEFGH\",\n" +
            "      \"name\": \"An active user\",\n" +
            "      \"deleted\": false,\n" +
            "      \"color\": \"e06b56\",\n" +
            "      \"real_name\": \"John Doe\",\n" +
            "      \"tz\": \"America/Chicago\",\n" +
            "      \"tz_label\": \"Central Standard Time\",\n" +
            "      \"tz_offset\": -21600,\n" +
            "      \"profile\": {\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"phone\": \"123-456-7890\",\n" +
            "        \"status_text\": \"Office\",\n" +
            "        \"status_emoji\": \":office:\",\n" +
            "        \"first_name\": \"John\",\n" +
            "        \"last_name\": \"Doe\",\n" +
            "        \"title\": \"Johndoe Doejohn\",\n" +
            "        \"real_name\": \"John Doe\",\n" +
            "        \"display_name\": \"john\",\n" +
            "        \"real_name_normalized\": \"John Doe\",\n" +
            "        \"display_name_normalized\": \"john\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_admin\": false,\n" +
            "      \"is_owner\": false,\n" +
            "      \"is_primary_owner\": false,\n" +
            "      \"is_restricted\": false,\n" +
            "      \"is_ultra_restricted\": false,\n" +
            "      \"is_bot\": false,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"has_2fa\": true,\n" +
            "      \"presence\": \"away\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"cache_ts\": 1234567890\n" +
            "}";

    public static final String RESPONSE_NO_MFA = "{\n" +
            "  \"ok\": true,\n" +
            "  \"members\": [\n" +
            "    {\n" +
            "      \"id\": \"A1BCDEFGH\",\n" +
            "      \"team_id\": \"A2BCDEFGH\",\n" +
            "      \"name\": \"a-bot\",\n" +
            "      \"deleted\": false,\n" +
            "      \"color\": \"827327\",\n" +
            "      \"real_name\": \"a-bot\",\n" +
            "      \"tz\": \"America/Los_Angeles\",\n" +
            "      \"tz_label\": \"Pacific Standard Time\",\n" +
            "      \"tz_offset\": -28800,\n" +
            "      \"profile\": {\n" +
            "        \"bot_id\": \"A1BCDEFGH\",\n" +
            "        \"api_app_id\": \"A1BCDEFGH\",\n" +
            "        \"always_active\": false,\n" +
            "        \"real_name\": \"a-bot\",\n" +
            "        \"display_name\": \"\",\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"real_name_normalized\": \"abot\",\n" +
            "        \"display_name_normalized\": \"\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_admin\": false,\n" +
            "      \"is_owner\": false,\n" +
            "      \"is_primary_owner\": false,\n" +
            "      \"is_restricted\": false,\n" +
            "      \"is_ultra_restricted\": false,\n" +
            "      \"is_bot\": true,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"presence\": \"away\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"B1BCDEFGH\",\n" +
            "      \"team_id\": \"B2BCDEFGH\",\n" +
            "      \"name\": \"A deleted user\",\n" +
            "      \"deleted\": true,\n" +
            "      \"profile\": {\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"real_name\": \"a-deleted-user\",\n" +
            "        \"display_name\": \"\",\n" +
            "        \"real_name_normalized\": \"chris\",\n" +
            "        \"display_name_normalized\": \"\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_bot\": false,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"presence\": \"away\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"C1BCDEFGH\",\n" +
            "      \"team_id\": \"C2BCDEFGH\",\n" +
            "      \"name\": \"An active user\",\n" +
            "      \"deleted\": false,\n" +
            "      \"color\": \"e06b56\",\n" +
            "      \"real_name\": \"John Doe\",\n" +
            "      \"tz\": \"America/Chicago\",\n" +
            "      \"tz_label\": \"Central Standard Time\",\n" +
            "      \"tz_offset\": -21600,\n" +
            "      \"profile\": {\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"phone\": \"123-456-7890\",\n" +
            "        \"status_text\": \"Office\",\n" +
            "        \"status_emoji\": \":office:\",\n" +
            "        \"first_name\": \"John\",\n" +
            "        \"last_name\": \"Doe\",\n" +
            "        \"title\": \"Johndoe Doejohn\",\n" +
            "        \"real_name\": \"John Doe\",\n" +
            "        \"display_name\": \"john\",\n" +
            "        \"real_name_normalized\": \"John Doe\",\n" +
            "        \"display_name_normalized\": \"john\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_admin\": false,\n" +
            "      \"is_owner\": false,\n" +
            "      \"is_primary_owner\": false,\n" +
            "      \"is_restricted\": false,\n" +
            "      \"is_ultra_restricted\": false,\n" +
            "      \"is_bot\": false,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"has_2fa\": false,\n" +
            "      \"presence\": \"away\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"cache_ts\": 1234567890\n" +
            "}";

    public static final String RESPONSE_DELETED_USER_NO_MFA = "{\n" +
            "  \"ok\": true,\n" +
            "  \"members\": [\n" +
            "    {\n" +
            "      \"id\": \"A1BCDEFGH\",\n" +
            "      \"team_id\": \"A2BCDEFGH\",\n" +
            "      \"name\": \"a-bot\",\n" +
            "      \"deleted\": false,\n" +
            "      \"color\": \"827327\",\n" +
            "      \"real_name\": \"a-bot\",\n" +
            "      \"tz\": \"America/Los_Angeles\",\n" +
            "      \"tz_label\": \"Pacific Standard Time\",\n" +
            "      \"tz_offset\": -28800,\n" +
            "      \"profile\": {\n" +
            "        \"bot_id\": \"A1BCDEFGH\",\n" +
            "        \"api_app_id\": \"A1BCDEFGH\",\n" +
            "        \"always_active\": false,\n" +
            "        \"real_name\": \"a-bot\",\n" +
            "        \"display_name\": \"\",\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"real_name_normalized\": \"abot\",\n" +
            "        \"display_name_normalized\": \"\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_admin\": false,\n" +
            "      \"is_owner\": false,\n" +
            "      \"is_primary_owner\": false,\n" +
            "      \"is_restricted\": false,\n" +
            "      \"is_ultra_restricted\": false,\n" +
            "      \"is_bot\": true,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"presence\": \"away\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"B1BCDEFGH\",\n" +
            "      \"team_id\": \"B2BCDEFGH\",\n" +
            "      \"name\": \"A deleted user\",\n" +
            "      \"deleted\": true,\n" +
            "      \"profile\": {\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"real_name\": \"a-deleted-user\",\n" +
            "        \"display_name\": \"\",\n" +
            "        \"real_name_normalized\": \"chris\",\n" +
            "        \"display_name_normalized\": \"\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_bot\": false,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"has_2fa\": false,\n" +
            "      \"presence\": \"away\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"C1BCDEFGH\",\n" +
            "      \"team_id\": \"C2BCDEFGH\",\n" +
            "      \"name\": \"An active user\",\n" +
            "      \"deleted\": false,\n" +
            "      \"color\": \"e06b56\",\n" +
            "      \"real_name\": \"John Doe\",\n" +
            "      \"tz\": \"America/Chicago\",\n" +
            "      \"tz_label\": \"Central Standard Time\",\n" +
            "      \"tz_offset\": -21600,\n" +
            "      \"profile\": {\n" +
            "        \"avatar_hash\": \"aaabbbccc\",\n" +
            "        \"image_24\": \"https://avatars.slack-edge.com/2017-10-19/123_24.jpg\",\n" +
            "        \"image_32\": \"https://avatars.slack-edge.com/2017-10-19/123_32.jpg\",\n" +
            "        \"image_48\": \"https://avatars.slack-edge.com/2017-10-19/123_48.jpg\",\n" +
            "        \"image_72\": \"https://avatars.slack-edge.com/2017-10-19/123_72.jpg\",\n" +
            "        \"image_192\": \"https://avatars.slack-edge.com/2017-10-19/123_192.jpg\",\n" +
            "        \"image_512\": \"https://avatars.slack-edge.com/2017-10-19/123_512.jpg\",\n" +
            "        \"image_1024\": \"https://avatars.slack-edge.com/2017-10-19/123_1024.jpg\",\n" +
            "        \"image_original\": \"https://avatars.slack-edge.com/2017-10-19/123_original.jpg\",\n" +
            "        \"phone\": \"123-456-7890\",\n" +
            "        \"status_text\": \"Office\",\n" +
            "        \"status_emoji\": \":office:\",\n" +
            "        \"first_name\": \"John\",\n" +
            "        \"last_name\": \"Doe\",\n" +
            "        \"title\": \"Johndoe Doejohn\",\n" +
            "        \"real_name\": \"John Doe\",\n" +
            "        \"display_name\": \"john\",\n" +
            "        \"real_name_normalized\": \"John Doe\",\n" +
            "        \"display_name_normalized\": \"john\",\n" +
            "        \"team\": \"A2BCDEFGH\"\n" +
            "      },\n" +
            "      \"is_admin\": false,\n" +
            "      \"is_owner\": false,\n" +
            "      \"is_primary_owner\": false,\n" +
            "      \"is_restricted\": false,\n" +
            "      \"is_ultra_restricted\": false,\n" +
            "      \"is_bot\": false,\n" +
            "      \"updated\": 1234567890,\n" +
            "      \"is_app_user\": false,\n" +
            "      \"has_2fa\": true,\n" +
            "      \"presence\": \"away\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"cache_ts\": 1234567890\n" +
            "}";

    @Test
    public void testCheckWithNoIssues() throws Exception {
        checkAndExpectIssues(RESPONSE_ISSUEFREE, 0);
    }

    @Test
    public void testCheckWithMFADisabledOnActiveUser() throws Exception {
        checkAndExpectIssues(RESPONSE_NO_MFA, 1);
    }

    @Test
    public void testCheckWithMFADisabledOnDeletedUser() throws Exception {
        checkAndExpectIssues(RESPONSE_DELETED_USER_NO_MFA, 0);
    }

    private void checkAndExpectIssues(String response, int issueCount) throws IOException {
        MockWebServer server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(200).setBody(response));
        server.start();
        HttpUrl mockUrl = server.url("/");

        CheckConfiguration configuration = new CheckConfiguration(Maps.newHashMap(), getBaseConfiguration());

        try {
            SlackTeamCheck check = new SlackTeamCheck("test1", configuration, getHttpClient(), getObjectMapper());
            check.setUrl(mockUrl);

            check.check();

            assertEquals(check.getIssues().size(), issueCount);
        } finally {
            server.shutdown();
        }
    }

}