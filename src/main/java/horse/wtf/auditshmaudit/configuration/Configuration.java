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

package horse.wtf.auditshmaudit.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import horse.wtf.auditshmaudit.checks.Check;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class Configuration {

    private static final Logger LOG = LogManager.getLogger(Configuration.class);

    @JsonProperty("attic_folder")
    public String atticFolder;

    @JsonProperty("include")
    @Nullable
    public String include;

    @JsonProperty
    public List<Map<String, Object>> checks;

    public boolean isComplete() {
        return checks != null && !Strings.isNullOrEmpty(atticFolder);
    }

    public void setAtticFolder(String atticFolder) {
        this.atticFolder = atticFolder;
    }

    public void setInclude(@Nullable String include) {
        this.include = include;
    }

    public void setChecks(List<Map<String, Object>> checks) {
        this.checks = checks;
    }

}
