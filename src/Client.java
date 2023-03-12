import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Client {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public Client() {
        try {
            socket = new Socket("localhost", 20525);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getTime() {
        try {
            long clientTime = System.currentTimeMillis();
            outputStream.writeLong(clientTime);
            Date dateClientTime = new Date(clientTime);
            System.out.println("Client time sent: " + formatter.format(dateClientTime));

            long serverTime = inputStream.readLong();
            long clientReceiveTime = System.currentTimeMillis();
            long rtt = clientReceiveTime - clientTime;
            long adjustedTime = serverTime + rtt / 2;

            Date dateServerTime = new Date(serverTime);
            Date dateAdjustedTime = new Date(adjustedTime);

            System.out.println("Server time received: " + formatter.format(dateServerTime));
            System.out.println("Adjusted time: " + formatter.format(dateAdjustedTime));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Client client = new Client();
        String option = "";
        do {
            System.out.println("If you want to get the correct time, enter 1. If you want to exit, enter 0.");
            option = scanner.nextLine();
            switch (option) {
                case "1" -> client.getTime();
                case "0" -> {
                    System.out.println("Leaving the system...");
                    client.closeConnection();
                }
            }
        } while (!"0".equals(option));
    }
}