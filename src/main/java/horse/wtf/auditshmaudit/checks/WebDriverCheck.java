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

package horse.wtf.auditshmaudit.checks;

import horse.wtf.auditshmaudit.configuration.CheckConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;

public abstract class WebDriverCheck extends Check {

    private static final Logger LOG = LogManager.getLogger(WebDriverCheck.class);

    protected WebDriverCheck(String id, CheckConfiguration configuration) {
        super(id, configuration);
    }

    public void saveScreenshotAndSource(String name, byte[] screenshotBytes, byte[] sourceBytes) {
        saveScreenshotAndSource(name, screenshotBytes, sourceBytes, DateTime.now());
    }

    public void saveScreenshotAndSource(String name, byte[] screenshotBytes, byte[] sourceBytes, DateTime timestamp) {
        // Screenshot.
        try {
            File screenshot = getAttic().writeFile(screenshotBytes, name + ".png", timestamp);

            LOG.info("Screenshot written to [{}].", screenshot.getCanonicalPath());
        } catch (IOException e) {
            LOG.error("Could not write screenshot.", e);
        }

        // Source.
        try {
            File source = getAttic().writeFile(sourceBytes, name + ".html", timestamp);

            LOG.info("Page source written to [{}], [{}].", source.getCanonicalPath());
        } catch (IOException e) {
            LOG.error("Could not write page source.", e);
        }
    }

}
