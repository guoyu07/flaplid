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

package horse.wtf.flaplid.uplink;

import com.google.common.collect.Maps;
import horse.wtf.flaplid.Issue;

import java.util.Map;

public class Notification {

    public enum TYPE {
        ISSUE, RUN_ISSUE_COUNT, RUN_META, RUN_CHECK_EXCEPTION
    }

    public enum FIELD {
        MESSAGE_TYPE,
        ISSUE_COUNT,
        RUN_DURATION_MS,
        TOTAL_CHECKS_EXECUTED,
        TOTAL_CHECKS_DISABLED,
        TOTAL_EXCEPTIONS,
        CHECK_SEVERITY,
        CHECK_ID,
        CHECK_NAME
    }

    private final String message;
    private final Map<FIELD, Object> fields;

    public Notification(Issue issue) {
        this.message = "[ISSUE] " + issue.getMessage();

        // TODO add severity, id, name

        this.fields = Maps.newHashMap();
        this.fields.put(FIELD.MESSAGE_TYPE, TYPE.ISSUE);
        this.fields.put(FIELD.CHECK_SEVERITY, issue.getCheck().getSeverity());
        this.fields.put(FIELD.CHECK_ID, issue.getCheck().getCheckId());
        this.fields.put(FIELD.CHECK_NAME, issue.getCheck().getFullCheckIdentifier());
    }

    public Notification(TYPE type, String message) {
        this.message = message;

        this.fields = Maps.newHashMap();
        this.fields.put(FIELD.MESSAGE_TYPE, type);
    }

    public String getMessage() {
        return message;
    }

    public void addField(FIELD field, Object value) {
        this.fields.put(field, value);
    }

    public void addFields(Map<FIELD, Object> fields) {
        this.fields.putAll(fields);
    }

    public Map<FIELD, Object> getFields() {
        return Maps.newHashMap(fields);
    }

}
