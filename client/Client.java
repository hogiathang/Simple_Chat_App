import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Client Dùng để xử lý ứng dụng bên phía người dùng thông qua
 * việc tạo kết nối socket đến server, ứng dụng sau đó sẽ sinh ra 2 luồng con
 * input dùng để lắng nghe tin nhắn từ server và output dùng để gởi tin nhắn lên server
 */
public class Client {
    // Static attributes:
    /**
     *  @Param SERVER_IP: Địa chỉ IP thiết bị
     *  @Param SERVER_PORT: Địa chỉ port thiết bị
     */
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 6789;

    // Instance attributes:
    /**
     * @Param socket: Socket dùng để kết nối đến server
     * @Param clientThreads: Danh sách các thread dùng để xử lý với phía server. Bao gồm input message: Handle tin nhắn từ server. Output message: Handle tin nhắn từ người dùng.
     */
    private Socket socket = null;
    private final List<Thread> clientThreads = new ArrayList<>();

    public void connectToServer() throws InterruptedException{
        try {
            this.socket = new Socket(Client.SERVER_IP, Client.SERVER_PORT);
            
            this.clientThreads.add(new Thread(new InputMessage(this.socket.getInputStream())));
            this.clientThreads.add(new Thread(new OutputMessage(this.socket.getOutputStream())));
            
            for (Thread clientThread: this.clientThreads) {
                clientThread.start();
            }

            // Thread này dùng để giữ cho chương trình chạy. Tránh tình trạng orphan thread
            while (true) {Thread.sleep(1000);}

        } catch (IOException e) {
            for (Thread thread: this.clientThreads) {
                thread.join();
            }
        }
    }

    // Main class
    public static void main(String[] args) {
        try {
            new Client().connectToServer();
        } catch (InterruptedException | NoSuchElementException e) {
            System.out.println("Chương trình đã bị tạm dừng");
        }
    }
}


class InputMessage implements  Runnable {
    private final InputStream serverMessage;

    public InputMessage(InputStream input) {
        this.serverMessage = input;
    }

    @Override
    public void run() {
        try {
            // Đọc tin nhắn từ phía server
            BufferedReader message = new BufferedReader(new InputStreamReader(serverMessage));
            String line;
            while ((line = message.readLine()) != null) {
                System.out.println(line);
            }
        } catch(IOException e) {
            System.out.println("Socket đã đóng");
        }
    }
} 


class OutputMessage implements Runnable {
    private final OutputStream clientMessage;

    public OutputMessage(OutputStream clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        StringBuilder inputStream = new StringBuilder();
        try (Scanner scanner = new Scanner(System.in)){
            while (true) {
                // Gởi tin nhắn lên server
                inputStream.append(scanner.nextLine());
                inputStream.append("\n");
                clientMessage.write(inputStream.toString().getBytes());
            }
        }
        catch(IOException e) {
            System.out.println("Socket đã đóng");  
        }
    }
}