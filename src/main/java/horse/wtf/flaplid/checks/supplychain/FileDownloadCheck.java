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

package horse.wtf.flaplid.checks.supplychain;

import horse.wtf.flaplid.checks.Check;
import horse.wtf.flaplid.configuration.CheckConfiguration;

public class FileDownloadCheck extends Check {

    public FileDownloadCheck(String id, CheckConfiguration configuration) {
        super(id, configuration);
    }

    @Override
    protected void check() {

    }

    @Override
    public String getCheckType() {
        return null;
    }

    @Override
    public boolean isConfigurationComplete() {
        return false;
    }

}
