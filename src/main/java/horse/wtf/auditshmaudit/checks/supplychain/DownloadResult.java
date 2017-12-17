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

import java.io.File;

public class DownloadResult {

    private final File downloadedFile;
    private final String downloadedFilePath;
    private final byte[] downloadedBytes;


    public DownloadResult(File downloadedFile, String downloadedFilePath, byte[] downloadedBytes) {
        this.downloadedFile = downloadedFile;
        this.downloadedFilePath = downloadedFilePath;
        this.downloadedBytes = downloadedBytes;
    }

    public File getDownloadedFile() {
        return downloadedFile;
    }

    public String getDownloadedFilePath() {
        return downloadedFilePath;
    }

    public byte[] getDownloadedBytes() {
        return downloadedBytes;
    }

}
