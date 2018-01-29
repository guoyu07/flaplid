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

package horse.wtf.flaplid.uplink.graylog;

import horse.wtf.flaplid.uplink.Notification;
import horse.wtf.flaplid.uplink.Uplink;
import org.graylog2.gelfclient.GelfConfiguration;
import org.graylog2.gelfclient.GelfMessage;
import org.graylog2.gelfclient.GelfTransports;
import org.graylog2.gelfclient.transport.GelfTransport;

import java.net.InetSocketAddress;

public class GraylogUplink implements Uplink {

    private static final String SOURCE = "flaplid";

    private final String flaplidId;
    private final String runId;

    private final GelfTransport gelfTransport;

    public GraylogUplink(GraylogAddress address, String flaplidId, String runId) {
        this.flaplidId = flaplidId;
        this.runId = runId;

        this.gelfTransport = GelfTransports.create(new GelfConfiguration(new InetSocketAddress(address.getHost(), address.getPort()))
                .transport(GelfTransports.TCP)
                .queueSize(2048)
                .connectTimeout(10000)
                .reconnectDelay(1000)
                .tcpNoDelay(true)
                .sendBufferSize(32768));
    }

    @Override
    public void notify(Notification notification) {
        GelfMessage gelf = new GelfMessage("flaplid: " + notification.getMessage(), SOURCE);
        gelf.addAdditionalField("flaplid_sensor_id", this.flaplidId);
        gelf.addAdditionalField("flaplid_run_id", this.runId);

        this.gelfTransport.trySend(gelf);
    }

}
