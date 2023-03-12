import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

            // Set local system time
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateAdjustedTime);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            int second = calendar.get(Calendar.SECOND);
            String dateTime = String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
            String command = "cmd /c date " + dateTime.substring(0, 10) + " && time " + dateTime.substring(11);
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getTimePeriodically() {
        while (true) {
            getTime();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
            System.out.println("if you want to correct your time only once, enter 1. If you want to run this algorithm every 1 second, enter 2. If you want to quit, enter 0.");
            option = scanner.nextLine();
            switch (option) {
                case "1" -> client.getTime();
                case "2" -> client.getTimePeriodically();
                case "0" -> {
                    System.out.println("Leaving the system...");
                    client.closeConnection();
                }
            }
        } while (!"0".equals(option));
    }
}