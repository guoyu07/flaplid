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

package horse.wtf.auditshmaudit.attic;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.IOException;

public class Attic {

    private final String id;
    private final String folder;
    private final String checkFolder;

    public Attic(String id, String atticFolder) {
        this.id = id;
        this.folder = atticFolder;
        this.checkFolder = atticFolder + "/" + id;

        // Create folders if required.
        try {
            ensureFolder(atticFolder);
            ensureFolder(checkFolder);
        } catch(Exception e) {
            throw new RuntimeException("Error during attic initialization for check [" + id + "]", e);
        }
    }

    public File writeFile(byte[] bytes) throws IOException {
        File file = new File(checkFolder + "/" + DateTime.now().toString(DateTimeFormat.forPattern("yyyy-mm-dd_HH-mm-ss")) + ".atticfile");

        ByteSink sink = Files.asByteSink(file);
        sink.write(bytes);

        return file;
    }

    private void ensureFolder(String name) throws IOException {
        File folder = new File(name);
        if (!folder.isDirectory()) {
            if (!folder.mkdir()) {
                throw new RuntimeException("Could not create attic folder at [" + folder.getCanonicalPath() + "].");
            }
        }
    }

}
