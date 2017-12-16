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
import horse.wtf.auditshmaudit.configuration.Configuration;
import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.helpers.PhantomJS;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class WebsiteDownloadCheck extends Check {

    private static final Logger LOG = LogManager.getLogger(WebsiteDownloadCheck.class);

    private static final String TYPE = "website_download";

    private static final String C_URL = "url";
    private static final String C_CSS_SELECTOR = "css_selector";
    private static final String C_CSS_SELECTOR_INDEX = "css_selector_index";
    private static final String C_EXPECTED_SHA256_CHECKSUM = "expected_sha256_checksum";
    private static final String C_ARCHIVE_ALL_FILES = "archive_all_files";
    private static final String C_ARCHIVE_MISMATCHED_FILES = "archive_mismatched_files";

    private final String id;
    private final Configuration configuration;
    private final OkHttpClient httpClient;

    public WebsiteDownloadCheck(String id, Configuration configuration, OkHttpClient httpClient) {
        super(configuration);

        this.id = id;
        this.configuration = configuration;
        this.httpClient = httpClient;
    }

    @Override
    protected List<Issue> check() {
        String cssSelector = configuration.getString(this, C_CSS_SELECTOR);
        int selectorIndex = configuration.getInt(this, C_CSS_SELECTOR_INDEX);
        String url = configuration.getString(this, C_URL);

        PhantomJSDriver driver = PhantomJS.buildDriver(PhantomJS.randomUserAgent());

        String downloadLink;
        try {
            LOG.info("Opening [{}] to get download link via CSS selector [{} (ix#{})].", url, cssSelector, selectorIndex);
            driver.get(url);
            List<WebElement> elements = driver.findElements(By.cssSelector(cssSelector));

            if (elements.size() <= selectorIndex) {
                throw new RuntimeException("Requested CSS selector index [" + selectorIndex + "] but only found [" + elements.size() + "] elements. (remember: index starts at 0)");
            }

            WebElement element = elements.get(selectorIndex);

            downloadLink = element.getAttribute("href");
            if(downloadLink == null) {
                throw new RuntimeException("Element #" + selectorIndex + " of [" + cssSelector + "] on [" + url + "] does not have a href attribute. Cannot follow for download.");
            }
        } catch (NoSuchElementException e) {
            throw new RuntimeException("Could not find any element at [" + cssSelector + "] on [" + url + "].");
        }

        // Follow link and download file to attic.
        File downloadedFile;
        String downloadedFilePath;
        byte[] downloadedBytes;
        try {
            LOG.info("Downloading file from [{}].", downloadLink);
            Response response = this.httpClient.newCall(
                    new Request.Builder()
                            .url(downloadLink)
                            .addHeader("User-Agent", PhantomJS.randomUserAgent())
                    .build())
                    .execute();

            if(response.code() != 200) {
                throw new RuntimeException("Excepted HTTP response code [200] but got [" + response.code() + "].");
            }

            downloadedBytes = response.body().bytes();
            downloadedFile = getAttic().writeFile(downloadedBytes);
            downloadedFilePath = downloadedFile.getCanonicalPath();
            response.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not download file.", e);
        }

        // Calculate and compare checksum.
        String expectedChecksum = configuration.getString(this, C_EXPECTED_SHA256_CHECKSUM);
        LOG.info("Completed download. Comparing checksums. Expecting checksum [{}]", expectedChecksum);

        String checksum = Hashing.sha256().hashBytes(downloadedBytes).toString();
        if (checksum.equals(expectedChecksum)) {
            // Checksums match. Delete the file if no archival was configured.
            LOG.info("Checksums match. ({}=={})", expectedChecksum, checksum);

            if(configuration.getBoolean(this, C_ARCHIVE_ALL_FILES)) {
               LOG.info("Configuration requests to keep all files. ({}:true) Not deleting downloaded file from attic.", C_ARCHIVE_ALL_FILES);
            } else {
                LOG.info("Deleting downloaded file from attic as requested. ({}:false)", C_ARCHIVE_ALL_FILES);
                if(!downloadedFile.delete()) {
                    LOG.error("Could not delete file at [{}].", downloadedFilePath);
                }
            }
        } else {
            // Checksums do not match!
            LOG.warn("Checksums do not match! ({}!={})", expectedChecksum, checksum);

            if(configuration.getBoolean(this, C_ARCHIVE_MISMATCHED_FILES)) {
                LOG.info("Configuration requests to keep mismatched files. ({}:true) Not deleting downloaded file from attic.", C_ARCHIVE_MISMATCHED_FILES);
            } else {
                LOG.info("Deleting downloaded file from attic as requested. ({}:false)", C_ARCHIVE_MISMATCHED_FILES);
                if(!downloadedFile.delete()) {
                    LOG.error("Could not delete file at [{}].", downloadedFilePath);
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
    public String getCheckId() {
        return this.id;
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean disabled() {
        return !configuration.isCheckEnabled(this);
    }

    @Override
    public boolean configurationComplete() {
        return configuration.isCheckConfigurationComplete(this, Arrays.asList(
                C_URL,
                C_CSS_SELECTOR,
                C_EXPECTED_SHA256_CHECKSUM,
                C_ARCHIVE_ALL_FILES,
                C_ARCHIVE_MISMATCHED_FILES
        ));
    }

}
