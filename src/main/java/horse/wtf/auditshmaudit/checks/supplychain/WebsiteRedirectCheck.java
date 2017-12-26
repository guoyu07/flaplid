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

package horse.wtf.auditshmaudit.checks.supplychain;

import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.WebDriverCheck;
import horse.wtf.auditshmaudit.checks.supplychain.helpers.PhantomJS;
import horse.wtf.auditshmaudit.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.Arrays;

public class WebsiteRedirectCheck extends WebDriverCheck {

    private static final Logger LOG = LogManager.getLogger(WebsiteDownloadCheck.class);

    public static final String TYPE = "website_redirect";

    private static final String C_URL = "url";
    private static final String C_EXPECTED_FINAL_TARGET = "expected_final_target";
    private static final String C_ARCHIVE_MISMATCHES = "archive_mismatches";
    private static final String C_ARCHIVE_MATCHES = "archive_matches";

    private final Configuration configuration;

    public WebsiteRedirectCheck(String id, Configuration configuration) {
        super(id, configuration);

        this.configuration = configuration;
    }

    @Override
    protected void check() {
        String url = configuration.getString(this, C_URL);
        String expectedTarget = configuration.getString(this, C_EXPECTED_FINAL_TARGET);

        PhantomJSDriver driver = PhantomJS.buildDriver(PhantomJS.randomUserAgent());
        driver.get(url);

        String destination = driver.getCurrentUrl();

        byte[] screenshotBytes = driver.getScreenshotAs(OutputType.BYTES);
        byte[] sourceBytes = driver.getPageSource().getBytes();

        if(expectedTarget.equals(destination)) {
            if(configuration.getBoolean(this, C_ARCHIVE_MATCHES)) {
                saveScreenshotAndSource("destination", screenshotBytes, sourceBytes);
            } else {
                LOG.debug("Not storing screenshot and page source code as requested. ({}:false)", C_ARCHIVE_MATCHES);
            }

            LOG.debug("We have been redirected to expected target [{}].", expectedTarget);
        } else {
            if (configuration.getBoolean(this, C_ARCHIVE_MISMATCHES)) {
                saveScreenshotAndSource("destination", screenshotBytes, sourceBytes);
            } else {
                LOG.debug("Not storing screenshot and page source code as requested. ({}:false)", C_ARCHIVE_MISMATCHES);
            }

            LOG.warn("We have been redirected to URL [{}] that does not match the expected target [{}].", destination, expectedTarget);

            addIssue(new Issue(this, "We have been redirected to URL [{}] that does not match the expected target [{}].",
                    destination, expectedTarget));
        }
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean isConfigurationComplete() {
        return configuration.isCheckConfigurationComplete(this, Arrays.asList(
                C_URL,
                C_EXPECTED_FINAL_TARGET,
                C_ARCHIVE_MATCHES,
                C_ARCHIVE_MISMATCHES
        ));
    }

}
