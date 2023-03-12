import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NTPClient {

    private static final int NTP_PORT = 123;
    private static final int NTP_PACKET_SIZE = 48;

    private final String serverHostname;

    public NTPClient(String serverHostname) {
        this.serverHostname = serverHostname;
    }

    public long getTime() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(serverHostname);
        byte[] buffer = new byte[NTP_PACKET_SIZE];

        // Initialize request packet
        buffer[0] = (byte) 0x1B;

        // Send request packet
        DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, address, NTP_PORT);
        socket.send(requestPacket);

        // Receive response packet
        DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
        socket.receive(responsePacket);

        // Calculate time from response packet
        long seconds = (long)(buffer[40] & 0x0FF) << 24 |
                (long)(buffer[41] & 0x0FF) << 16 |
                (long)(buffer[42] & 0x0FF) << 8 |
                (long)(buffer[43] & 0x0FF);
        long timestamp = (seconds - 2208988800L) * 1000;

        // Close socket and return time
        socket.close();
        return timestamp;
    }
}