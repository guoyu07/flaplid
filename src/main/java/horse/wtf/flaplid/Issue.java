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

import horse.wtf.flaplid.checks.Check;

import java.util.regex.Matcher;

public class Issue {

    private final Check check;
    private final String message;

    public Issue(Check check, String message, Object... messageVariables) {
        this.check = check;

        if(messageVariables == null || messageVariables.length == 0) {
            this.message = message;
        } else {
            for (Object messageVariable : messageVariables) {
                message = message.replaceFirst("\\{}", safeToString(messageVariable));
            }
            this.message = message;
        }
    }

    public Check getCheck() {
        return check;
    }

    public String getMessage() {
        return message;
    }

    private String safeToString(Object x) {
        if (x == null) {
            return "null";
        } else {
            return Matcher.quoteReplacement(x.toString());
        }
    }

}
