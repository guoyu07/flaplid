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

import com.google.inject.Inject;
import horse.wtf.auditshmaudit.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.helpers.PhantomJS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.List;

public class WebsiteDownloadCheck extends Check {

    private static final Logger LOG = LogManager.getLogger(WebsiteDownloadCheck.class);

    private static final String NAME = "Supply Chain: Website Download";

    private final Configuration configuration;

    @Inject
    public WebsiteDownloadCheck(Configuration configuration) {
        this.configuration = configuration;
    }

    /*
     * TODO:
     *   - store whole source somewhere if enabled
     *   - make everything configurable
     *   - test that JS is actually executed and working (against local graylog?)
     */

    @Override
    protected List<Issue> check() {
        PhantomJSDriver driver = PhantomJS.buildDriver(PhantomJS.randomUserAgent());

        driver.get("http://localhost:3000/download");
        WebElement element = driver.findElement(By.cssSelector("#tgz-download"));

        LOG.info("dl: {}", element.getAttribute("href"));

        // Follow link and download file to local tmp folder.

        // Calculate and compare checksum.

        if (!true) {
            // Checksums do not match!

            // Generate UUID and report issue with hint that sample was stored.
            // Store file and source code in samples/uuid.
        }

        return issues();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean disabled() {
        return !configuration.isCheckWebsiteDownloadsEnabled();
    }

    @Override
    public boolean configurationComplete() {
        return true;
    }

}
