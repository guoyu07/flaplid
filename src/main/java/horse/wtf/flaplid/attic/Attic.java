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

package horse.wtf.flaplid.attic;

import com.google.common.io.ByteSink;
import com.google.common.io.Files;
import horse.wtf.flaplid.checks.Check;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.IOException;

public class Attic {

    private final Check check;
    private final String folder;
    private final String checkTypeFolder;
    private final String checkIdFolder;

    public Attic(Check check, String atticFolder) {
        this.check = check;
        this.folder = atticFolder;
        this.checkTypeFolder = atticFolder + "/" + check.getCheckType();
        this.checkIdFolder = checkTypeFolder + "/" + check.getCheckId();

        // Create folders if required.
        try {
            ensureFolder(atticFolder);
            ensureFolder(checkTypeFolder);
            ensureFolder(checkIdFolder);
        } catch(Exception e) {
            throw new RuntimeException("Error during attic initialization for check [" + check.getFullCheckIdentifier() + "]", e);
        }
    }

    public File writeFile(byte[] bytes, String ending, DateTime timestamp) throws IOException {
        File file = new File(checkIdFolder + "/" + timestamp.toString(DateTimeFormat.forPattern("yyyy-MM-dd_HH-mm-ss")) + "." + ending);

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