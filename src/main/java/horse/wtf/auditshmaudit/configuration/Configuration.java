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

import java.util.List;
import java.util.Map;

public class Configuration {

    private static final Logger LOG = LogManager.getLogger(Configuration.class);

    @JsonProperty("attic_folder")
    public String atticFolder;

    @JsonProperty
    public List<Map<String, Object>> checks;

    public boolean isComplete() {
        return checks != null && !Strings.isNullOrEmpty(atticFolder);
    }

    public String getString(Check check, String key) {
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

    public Boolean getBoolean(Check check, String key) {
        Object o = getObject(check, key);

        return o == null ? false : (Boolean) o;
    }

    public Integer getInt(Check check, String key) {
        Object o = getObject(check, key);

        return o == null ? null : (Integer) o;
    }

    public List<String> getListOfStrings(Check check, String key) {
        Object o = getObject(check, key);

        return o == null ? null : (List) o;
    }

    public List<PortAndProtocol> getListOfPortsAndProtocols(Check check, String key) {
        List<String> defs = getListOfStrings(check, key);

        ImmutableList.Builder<PortAndProtocol> ports = new ImmutableList.Builder<>();

        for (String def : defs) {
            try {
                if(!def.contains("/")) {
                    throw new RuntimeException("Malformed port entry.");
                }

                String[] parts = def.split("/");
                ports.add(new PortAndProtocol(parts[0], Integer.parseInt(parts[1])));
            } catch(Exception e) {
                LOG.error("Could not parse critical port. Skipping.", e);
                continue;
            }
        }

        return ports.build();
    }

    public Object getObject(Check check, String key) {
        return findCheckConfig(check).getOrDefault(key, null);
    }

    private Map<String, Object> findCheckConfig(Check check) {
        for (Map<String, Object> checkConfig : checks) {
            if(!checkConfig.containsKey("type") || !checkConfig.containsKey("id")) {
                throw new RuntimeException("Missing type or ID for check [" + check.getFullCheckIdentifier() + "].");
            }

            String type = (String) checkConfig.get("type");
            String id = (String) checkConfig.get("id");

            if (type.equals(check.getCheckType()) && id.equals(check.getCheckId())) {
                return checkConfig;
            }
        }

        throw new RuntimeException("No config for check [" + check.getFullCheckIdentifier() + "] found.");
    }

    public Boolean isCheckEnabled(Check check) {
        return getBoolean(check, "enabled");
    }

    public boolean isCheckConfigurationComplete(Check check, List<String> requiredKeys) {
        for (String requiredKey : requiredKeys) {
            if (getObject(check, requiredKey) == null) {
                LOG.error("Requested config variable not set: {}.{}", check, requiredKey);
                return false;
            }
        }

        return true;
    }

    public class PortAndProtocol {

        private final String protocol;
        private final int port;

        public PortAndProtocol(String protocol, int port) {
            this.protocol = protocol;
            this.port = port;
        }

        public String getProtocol() {
            return protocol;
        }

        public int getPort() {
            return port;
        }

        @Override
        public String toString() {
            return protocol + "/" + port;
        }

    }

}
