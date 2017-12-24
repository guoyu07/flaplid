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

import com.google.common.hash.Hashing;
import horse.wtf.auditshmaudit.checks.supplychain.helpers.DownloadResult;
import horse.wtf.auditshmaudit.checks.supplychain.helpers.FileDownloader;
import horse.wtf.auditshmaudit.checks.supplychain.helpers.PhantomJS;
import horse.wtf.auditshmaudit.configuration.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import okhttp3.OkHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.Arrays;
import java.util.List;

public class WebsiteDownloadCheck extends Check {

    private static final Logger LOG = LogManager.getLogger(WebsiteDownloadCheck.class);

    public static final String TYPE = "website_download";

    private static final String C_URL = "url";
    private static final String C_CSS_SELECTOR = "css_selector";
    private static final String C_CSS_SELECTOR_INDEX = "css_selector_index";
    private static final String C_EXPECTED_SHA256_CHECKSUM = "expected_sha256_checksum";
    private static final String C_ARCHIVE_MATCHED_FILES = "archive_matched_files";
    private static final String C_ARCHIVE_MISMATCHED_FILES = "archive_mismatched_files";

    private final Configuration configuration;
    private final OkHttpClient httpClient;

    public WebsiteDownloadCheck(String id, Configuration configuration, OkHttpClient httpClient) {
        super(id, configuration);

        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    @Override
    protected List<Issue> check() {
        String cssSelector = configuration.getString(this, C_CSS_SELECTOR);
        int selectorIndex = configuration.getInt(this, C_CSS_SELECTOR_INDEX);
        String url = configuration.getString(this, C_URL);

        PhantomJSDriver driver = PhantomJS.buildDriver(PhantomJS.randomUserAgent());
        WebElement element = PhantomJS.getElementFromSite(driver, cssSelector, selectorIndex, url);

        String downloadLink = element.getAttribute("href");
        if(downloadLink == null) {
            throw new RuntimeException("Element #" + selectorIndex + " of [" + cssSelector + "] on [" + url + "] does not have a href attribute. Cannot follow for download.");
        }

        // Follow link and download file to attic.
        DownloadResult downloadResult = FileDownloader.downloadFileToAttic(this.getAttic(), this.httpClient, downloadLink, DateTime.now());

        // Calculate and compare checksum.
        String expectedChecksum = configuration.getString(this, C_EXPECTED_SHA256_CHECKSUM);
        LOG.info("Completed download. Comparing checksums. Expecting checksum [{}]", expectedChecksum);

        String checksum = Hashing.sha256().hashBytes(downloadResult.getDownloadedBytes()).toString();
        if (checksum.equals(expectedChecksum)) {
            // Checksums match. Delete the file if no archival was configured.
            LOG.info("Checksums match. ({}=={})", expectedChecksum, checksum);

            if(configuration.getBoolean(this, C_ARCHIVE_MATCHED_FILES)) {
               LOG.info("Configuration requests to keep all files. ({}:true) Not deleting downloaded file from attic. File is at [{}].",
                       C_ARCHIVE_MATCHED_FILES, downloadResult.getDownloadedFilePath());
            } else {
                LOG.info("Deleting downloaded file from attic as requested. ({}:false)", C_ARCHIVE_MATCHED_FILES);
                if(!downloadResult.getDownloadedFile().delete()) {
                    LOG.error("Could not delete file at [{}].", downloadResult.getDownloadedFilePath());
                }
            }
        } else {
            // Checksums do not match!
            LOG.warn("Checksums do not match! ({}!={})", expectedChecksum, checksum);

            if(configuration.getBoolean(this, C_ARCHIVE_MISMATCHED_FILES)) {
                LOG.info("Configuration requests to keep mismatched files. ({}:true) Not deleting downloaded file from attic. File is at [{}]",
                        C_ARCHIVE_MISMATCHED_FILES, downloadResult.getDownloadedFilePath());
            } else {
                LOG.info("Deleting downloaded file from attic as requested. ({}:false)", C_ARCHIVE_MISMATCHED_FILES);
                if(!downloadResult.getDownloadedFile().delete()) {
                    LOG.error("Could not delete file at [{}].", downloadResult.getDownloadedFilePath());
                }
            }

            addIssue(new Issue(
                    this,
                    "Downloaded file from [{}] (via CSS selector [{} (ix#{})] on [{}}) does not match expected checksum [{}] but was [{}].",
                    downloadLink, cssSelector, selectorIndex, url, expectedChecksum, checksum
            ));
        }

        return issues();
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean configurationComplete() {
        return configuration.isCheckConfigurationComplete(this, Arrays.asList(
                C_URL,
                C_CSS_SELECTOR,
                C_EXPECTED_SHA256_CHECKSUM,
                C_ARCHIVE_MATCHED_FILES,
                C_ARCHIVE_MISMATCHED_FILES
        ));
    }

}
