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

package horse.wtf.auditshmaudit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class Version {

    private static final Logger LOG = LogManager.getLogger(Version.class);

    public String getVersionString() {
        Properties gitProperties = new Properties();
        Properties buildProperties = new Properties();

        try {
            gitProperties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
            buildProperties.load(getClass().getClassLoader().getResourceAsStream("build.properties"));

            return new StringBuilder(String.valueOf(gitProperties.get("git.build.version")))
                    .append(" built at [")
                    .append(String.valueOf(buildProperties.get("date"))).append("] from [")
                    .append(String.valueOf(gitProperties.get("git.commit.id.describe-short"))).append("]")
                    .toString();
        } catch (IOException e) {
            LOG.error("Could not load version information.", e);
            return "";
        }
    }

}
