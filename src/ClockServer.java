import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class ClockServer {

    NTPClient ntpClient = new NTPClient("br.pool.ntp.org"); // address of ntp server in Brazil

    private ServerSocket serverSocket;

    public ClockServer() {
        try {
            serverSocket = new ServerSocket(20525);
            System.out.println("The clock server is on...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptConnections() throws IOException {
        while (true) {
            Socket socket = serverSocket.accept();

            Thread thread = new Thread(() -> {
                try {
                    var inputStream = new DataInputStream(socket.getInputStream());
                    var outputStream = new DataOutputStream(socket.getOutputStream());

                    long clientTime = inputStream.readLong();
                    System.out.println("Client time received: " + clientTime);

                    // uses the ntpClient do get time from a ntp server
                    long serverTime = ntpClient.getTime();
                    outputStream.writeLong(serverTime);
                    System.out.println("Server time sent: " + serverTime);

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            thread.start();
        }
    }

    public static void main(String[] args) throws IOException {
        ClockServer clockServer = new ClockServer();
        clockServer.acceptConnections();
    }
}
