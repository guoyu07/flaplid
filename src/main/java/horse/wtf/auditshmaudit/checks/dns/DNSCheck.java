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

package horse.wtf.auditshmaudit.checks.dns;

import horse.wtf.auditshmaudit.Issue;
import horse.wtf.auditshmaudit.checks.Check;
import horse.wtf.auditshmaudit.configuration.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbill.DNS.*;

import java.util.Arrays;
import java.util.List;

public class DNSCheck extends Check {

    private static final Logger LOG = LogManager.getLogger(DNSCheck.class);

    private static final String C_DNS_SERVER = "dns_server";
    private static final String C_DNS_QUESTION = "dns_question";
    private static final String C_DNS_QUESTION_TYPE = "dns_question_type";
    private static final String C_EXPECTED_ANSWER = "expected_answer";

    public static final String TYPE = "dns";

    private final Configuration configuration;

    public DNSCheck(String id, Configuration configuration) {
        super(id, configuration);

        this.configuration = configuration;
    }

    // TODO make DNS server selection work and test with mutliple test (changing between tests)

    @Override
    protected List<Issue> check() {
        try {
            Lookup lookup = new Lookup(
                    configuration.getString(this, C_DNS_QUESTION),
                    Type.value(configuration.getString(this, C_DNS_QUESTION_TYPE).toUpperCase())
            );
            lookup.setResolver(new SimpleResolver(configuration.getString(this, C_DNS_SERVER)));

            Record[] records = lookup.run();

            if (!lookup.getErrorString().equals("successful")) { // meeehhhhh stone age libraries
                throw new RuntimeException("DNS lookup failed with error: " + lookup.getErrorString());
            }

            for (Record record : records) {
                switch(record.getType()) {
                    case Type.MX:
                        MXRecord mx = (MXRecord) record;
                        LOG.info(mx.getTarget().toString(false));
                        break;
                    case Type.A:
                        ARecord a = (ARecord) record;
                        LOG.info(a.getAddress().toString());
                        break;
                }
            }

        } catch(Exception e) {
            throw new RuntimeException("Could not run DNS lookup.", e);
        }
        return issues();
    }

    @Override
    public String getCheckType() {
        return TYPE;
    }

    @Override
    public boolean disabled() {
        return !configuration.isCheckEnabled(this);
    }

    @Override
    public boolean configurationComplete() {
        return configuration.isCheckConfigurationComplete(this, Arrays.asList(
                C_DNS_SERVER,
                C_DNS_QUESTION,
                C_DNS_QUESTION_TYPE,
                C_EXPECTED_ANSWER
        ));
    }
}
