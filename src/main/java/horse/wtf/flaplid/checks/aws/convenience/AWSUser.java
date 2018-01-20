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

package horse.wtf.flaplid.checks.aws.convenience;

import org.joda.time.DateTime;

import java.util.List;

public class AWSUser {

    private final String username;
    private final DateTime passwordLastUsed;
    private final DateTime createdAt;
    private final List<AWSAccessKey> accessKeys;
    private final int mfaDeviceCount;

    public AWSUser(String username, DateTime passwordLastUsed, DateTime createdAt, List<AWSAccessKey> accessKeys, int mfaDeviceCount) {
        this.username = username;
        this.passwordLastUsed = passwordLastUsed;
        this.createdAt = createdAt;
        this.accessKeys = accessKeys;
        this.mfaDeviceCount = mfaDeviceCount;
    }

    public String getUsername() {
        return username;
    }

    public DateTime getPasswordLastUsed() {
        return passwordLastUsed;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    public List<AWSAccessKey> getAccessKeys() {
        return accessKeys;
    }

    public int getMfaDeviceCount() {
        return mfaDeviceCount;
    }

    public boolean hasAPIKeys() {
        return false;
    }

}
