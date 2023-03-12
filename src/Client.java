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
    private static final int ADJUSTMENT_INTERVAL = 1000; // 1 second interval between clock adjustments
    private static final int ADJUSTMENT_STEP = 10000; // 10 seconds adjustment step

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

            // Get the correct time from the server
            long serverTime = inputStream.readLong();
            Date dateServerTime = new Date(serverTime);
            System.out.println("Server time received: " + formatter.format(dateServerTime));

            // Get current time after receiving response
            long receiveTime = System.currentTimeMillis();

            // Calculate RTT and clock deviation
            long rtt = receiveTime - clientTime;
            long clockDeviation = (serverTime - (clientTime + rtt/2));

            // Gradually adjust local system time
            long adjustmentAmount = Math.abs(clockDeviation) / ADJUSTMENT_STEP;
            long adjustmentSign = clockDeviation < 0 ? -1 : 1;
            long localTime = System.currentTimeMillis();

            for (int i = 0; i < adjustmentAmount; i++) {
                localTime += ADJUSTMENT_STEP * adjustmentSign;
                setSystemTime(localTime);
                try {
                    Thread.sleep(ADJUSTMENT_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Adjust final local system time
            long finalTime = System.currentTimeMillis() + clockDeviation;
            setSystemTime(finalTime);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void setSystemTime(long newTime) throws IOException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(newTime));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        System.out.println("New adjustment: " + new Date(newTime));
        String dateTime = String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
        String command = "sudo date -s '" + dateTime + "'";
        Process process = Runtime.getRuntime().exec(new String[]{"bash", "-c", command});
        try {
            process.waitFor();
        } catch (InterruptedException e) {
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
