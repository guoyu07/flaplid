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

package horse.wtf.flaplid.configuration;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import horse.wtf.flaplid.checks.Severity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CheckConfiguration {

    private static final Logger LOG = LogManager.getLogger(CheckConfiguration.class);

    private final Map<String, Object> config;
    private final String atticBaseFolderPath;

    private final static String C_TYPE = "type";
    private final static String C_ID = "id";
    private final static String C_SEVERITY = "severity";
    private final static String C_TAGS = "tags";
    private final static String C_ENABLED = "enabled";

    public CheckConfiguration(Map<String, Object> config, Configuration configuration) {
        this.config = config;
        this.atticBaseFolderPath = configuration.atticFolder;
    }

    public String getType() {
        return getString(C_TYPE);
    }

    public String getId() {
        return getString(C_ID);
    }

    public Severity getSeverity() {
        Severity fallback = Severity.EMERGENCY;
        String s = getString(C_SEVERITY);

        if (Strings.isNullOrEmpty(s)) {
            LOG.error("Check [{}] has no severity defined. Use any of {} in parameter \"severity\". Setting to {}.",
                    this.getId(), Severity.values(), fallback);
            return fallback;
        }

        try {
            return Severity.valueOf(s.toUpperCase());
        } catch(IllegalArgumentException e) {
            LOG.error("Check [{}] has invalid severity defined. Use any of {} in parameter \"severity\". Setting to {}.",
                    this.getId(), Severity.values(), fallback);
            return fallback;
        }
    }

    public boolean isEnabled() {
        return getBoolean(C_ENABLED);
    }

    public String getAtticBaseFolderPath() {
        return atticBaseFolderPath;
    }

    public String getString(String key) {
        Object o = getObject(key);

        if (o == null) {
            return null;
        }

        if(((String) o).trim().isEmpty()) {
            return null;
        } else {
            return ((String) o).trim();
        }
    }

    public Boolean getBoolean(String key) {
        Object o = getObject(key);

        return o == null ? false : (Boolean) o;
    }

    public Integer getInt(String key) {
        Object o = getObject(key);

        return o == null ? null : (Integer) o;
    }

    public List<String> getListOfStrings(String key) {
        Object o = getObject(key);

        return o == null ? null : (List) o;
    }

    public Object getObject(String key) {
        return config.getOrDefault(key, null);
    }

    public List<PortAndProtocol> getListOfPortsAndProtocols(String key) {
        List<String> defs = getListOfStrings(key);

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

    public boolean standardParametersAreComplete() {
        for (String requiredStandardParameter : Arrays.asList(C_TYPE, C_ID, C_SEVERITY, C_TAGS, C_ENABLED)) {
            if (getObject(requiredStandardParameter) == null) {
                LOG.error("Required standard config variable not set: {}", requiredStandardParameter);
                return false;
            }
        }

        return true;
    }

    public boolean isComplete(List<String> requiredKeys) {
        for (String requiredKey : requiredKeys) {
            if (getObject(requiredKey) == null) {
                LOG.error("Requested config variable not set: {}", requiredKey);
                return false;
            }
        }

        return true;
    }

    public List<String> getTags() {
        return getListOfStrings(C_TAGS);
    }

    public boolean hasARequestedTag(List<String> requestedTags) {
        if(getTags() == null) {
            return false;
        }

        for (String requestedTag : requestedTags) {
            if (getTags().contains(requestedTag)) {
                return true;
            }
        }

        return false;
    }

    public class PortAndProtocol {

        private final String protocol;
        private final int port;

        PortAndProtocol(String protocol, int port) {
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
