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

package horse.wtf.flaplid;

import com.beust.jcommander.Parameter;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

public class CLIArguments {

    @Parameter(names={"--config-file", "-c"}, required = true)
    private String configFilePath;

    @Parameter(names={"--tags", "-t"}, required = false)
    private String tags;

    public String getConfigFilePath() {
        return configFilePath;
    }

    public boolean hasTags() {
        return !getTags().isEmpty();
    }

    public ImmutableList<String> getTags() {
        if (tags == null || tags.isEmpty()) {
            return new ImmutableList.Builder<String>().build();
        }

        return ImmutableList.copyOf(Splitter.on(",").trimResults().splitToList(tags));
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

}
