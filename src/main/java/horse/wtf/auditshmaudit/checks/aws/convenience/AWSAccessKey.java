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

package horse.wtf.auditshmaudit.checks.aws.convenience;

import org.joda.time.DateTime;

public class AWSAccessKey {

    public enum STATUS {
        ACTIVE, INACTIVE
    }

    private final STATUS status;
    private final DateTime createDate;
    private final DateTime lastUsed;

    public AWSAccessKey(STATUS status, DateTime createDate, DateTime lastUsed) {
        this.status = status;
        this.createDate = createDate;
        this.lastUsed = lastUsed;
    }

    public STATUS getStatus() {
        return status;
    }

    public DateTime getCreateDate() {
        return createDate;
    }

    public DateTime getLastUsed() {
        return lastUsed;
    }

}
