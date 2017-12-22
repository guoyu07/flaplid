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

package horse.wtf.auditshmaudit.checks.supplychain.helpers;

import horse.wtf.auditshmaudit.attic.Attic;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public class FileDownloader {

    private static final Logger LOG = LogManager.getLogger(FileDownloader.class);

    public static DownloadResult downloadFileToAttic(Attic attic, OkHttpClient httpClient, String url, DateTime atticTimestamp) {
        File downloadedFile;
        String downloadedFilePath;
        byte[] downloadedBytes;

        try {
            LOG.info("Downloading file from [{}].", url);
            Response response = httpClient.newCall(
                    new Request.Builder()
                            .url(url)
                            .addHeader("User-Agent", PhantomJS.randomUserAgent())
                            .build())
                    .execute();

            if(response.code() != 200) {
                throw new RuntimeException("Excepted HTTP response code [200] but got [" + response.code() + "].");
            }

            downloadedBytes = response.body().bytes();
            downloadedFile = attic.writeFile(downloadedBytes, "download", atticTimestamp);
            downloadedFilePath = downloadedFile.getCanonicalPath();
            response.close();
        } catch (IOException e) {
            throw new RuntimeException("Could not download file.", e);
        }

        return new DownloadResult(downloadedFile, downloadedFilePath, downloadedBytes);
    }

}
