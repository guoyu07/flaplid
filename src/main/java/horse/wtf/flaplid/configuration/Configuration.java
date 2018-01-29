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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.sun.istack.internal.NotNull;
import horse.wtf.flaplid.uplink.graylog.GraylogAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class Configuration {

    private static final Logger LOG = LogManager.getLogger(Configuration.class);

    @JsonProperty("attic_folder")
    public String atticFolder;

    @JsonProperty("sensor_id")
    public String sensorId;

    @JsonProperty("graylog_address")
    private String graylogAddress;

    @JsonProperty("include")
    public String include;

    @JsonProperty
    public List<Map<String, Object>> checks;

    public boolean isComplete() {
        return checks != null
                && !Strings.isNullOrEmpty(sensorId)
                && !Strings.isNullOrEmpty(atticFolder);
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

    @Nullable
    public GraylogAddress getGraylogAddress() {
        if(Strings.isNullOrEmpty(graylogAddress)) {
            return null;
        }

        if(!graylogAddress.contains(":")) {
            throw new RuntimeException("Invalid graylog_address: [" + graylogAddress + "]. Remove configuration parameter to disable Graylog uplink.");
        }

        String[] parts = graylogAddress.split(":");

        if(parts.length != 2 || Strings.isNullOrEmpty(parts[0]) || Strings.isNullOrEmpty(parts[1])) {
            throw new RuntimeException("Invalid graylog_address: [" + graylogAddress + "]. Remove configuration parameter to disable Graylog uplink.");
        }

        try {
            return new GraylogAddress(parts[0], Integer.parseInt(parts[1]));
        } catch(NumberFormatException e) {
            throw new RuntimeException("Invalid port in graylog_address: <" + parts[1] + ">.", e);
        }
    }

}
