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
import com.google.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

@Singleton
public class Configuration {

    private static final Logger LOG = LogManager.getLogger(Configuration.class);

    @JsonProperty("attic_folder")
    public String atticFolder;

    @JsonProperty
    public Map<String, Map<String, Object>> checks;

    public boolean isComplete() {
        return checks != null && !Strings.isNullOrEmpty(atticFolder);
    }

    public String getString(String check, String key) {
        Object o = getObject(check, key);

        if (o == null) {
            return null;
        }

        if(((String) o).trim().isEmpty()) {
            return null;
        } else {
            return ((String) o).trim();
        }
    }

    public Boolean getBoolean(String check, String key) {
        Object o = getObject(check, key);

        return o == null ? false : (Boolean) o;
    }

    public Object getObject(String check, String key) {
        if(!checks.containsKey(check)) {
            return null;
        }

        return checks.get(check).getOrDefault(key, null);
    }

    public Boolean isCheckEnabled(String check) {
        return getBoolean(check, "enabled");
    }

    public boolean isCheckConfigurationComplete(String check, List<String> requiredKeys) {
        for (String requiredKey : requiredKeys) {
            if (getObject(check, requiredKey) == null) {
                // TODO make sure this works with new way of config structuring (needs ID and type)
                LOG.error("Requested config variable not set: {}.{}", check, requiredKey);
                return false;
            }
        }

        return true;
    }

}
