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
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.checks.WebDriverCheck;
import horse.wtf.auditshmaudit.checks.supplychain.helpers.PhantomJS;
import horse.wtf.auditshmaudit.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class WebsiteLinkTargetCheck extends WebDriverCheck {

    private static final Logger LOG = LogManager.getLogger(WebsiteLinkTargetCheck.class);

    public static final String TYPE = "website_link_target";

    private static final String C_URL = "url";
    private static final String C_CSS_SELECTOR = "css_selector";
    private static final String C_CSS_SELECTOR_INDEX = "css_selector_index";
    private static final String C_EXPECTED_TARGET = "expected_target";
    private static final String C_ARCHIVE_MISMATCHES = "archive_mismatches";
    private static final String C_ARCHIVE_MATCHES = "archive_matches";

    private final Configuration configuration;

    public WebsiteLinkTargetCheck(String id, Configuration configuration) {
        super(id, configuration);

        this.configuration = configuration;
    }

    @Override
    protected void check() {
        String cssSelector = configuration.getString(this, C_CSS_SELECTOR);
        int selectorIndex = configuration.getInt(this, C_CSS_SELECTOR_INDEX);
        String url = configuration.getString(this, C_URL);
        String expectedTarget = configuration.getString(this, C_EXPECTED_TARGET);

        PhantomJSDriver driver = PhantomJS.buildDriver(PhantomJS.randomUserAgent());
        WebElement element = PhantomJS.getElementFromSite(driver, cssSelector, selectorIndex, url);

        // Remove all link targets to avoid sending PhantomJS to window hell on a target="_blank".
        driver.executeScript("var links = document.links, i, length; for (i = 0, length = links.length; i < length; i++) { links[i].target == '_blank' && links[i].removeAttribute('target'); }");

        byte[] sourceScreenshotBytes = driver.getScreenshotAs(OutputType.BYTES);
        byte[] sourceSourceBytes = driver.getPageSource().getBytes();

        LOG.debug("About to click on link with href [{}] and target [{}].", element.getAttribute("href"), element.getAttribute("target"));
        element.click();
        LOG.debug("Clicked.");

        String destination = driver.getCurrentUrl();

        byte[] destinationScreenshotBytes = driver.getScreenshotAs(OutputType.BYTES);
        byte[] destinationSourceBytes = driver.getPageSource().getBytes();

        if(expectedTarget.equals(destination)) {
            if(configuration.getBoolean(this, C_ARCHIVE_MATCHES)) {
                saveScreenshotAndSource("source", sourceScreenshotBytes, sourceSourceBytes);
                saveScreenshotAndSource("destination", destinationScreenshotBytes, destinationSourceBytes);
            } else {
                LOG.debug("Not storing screenshots and page source codes as requested. ({}:false)", C_ARCHIVE_MATCHES);
            }

            LOG.debug("We have been redirected to expected target [{}].", expectedTarget);
        } else {
            if (configuration.getBoolean(this, C_ARCHIVE_MISMATCHES)) {
                saveScreenshotAndSource("source", sourceScreenshotBytes, sourceSourceBytes);
                saveScreenshotAndSource("destination", destinationScreenshotBytes, destinationSourceBytes);
            } else {
                LOG.debug("Not storing screenshots and page source codes as requested. ({}:false)", C_ARCHIVE_MISMATCHES);
            }

            LOG.warn("We have been redirected to URL [{}] that does not match the expected target [{}].", destination, expectedTarget);

            addIssue(new Issue(this, "We have been redirected to URL [{}] that does not match the expected target [{}].",
                    destination, expectedTarget));
        }

        return;
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean isConfigurationComplete() {
        return configuration.isCheckConfigurationComplete(this, Arrays.asList(
                C_URL,
                C_CSS_SELECTOR,
                C_CSS_SELECTOR_INDEX,
                C_EXPECTED_TARGET,
                C_ARCHIVE_MATCHES,
                C_ARCHIVE_MISMATCHES
        ));
    }

}
