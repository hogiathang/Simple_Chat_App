import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final Server severInstance = null;
    private ServerSocket serverSocket = null;
    private final List<Thread> clientThreads = new ArrayList<>();
    private final List<Client> clients = new ArrayList<>();
    private Server() {}

    public static Server getInstance() {
        if (Server.severInstance == null) {
            return new Server();
        } else {
            return Server.severInstance;
        }
    }

    public void startServer(int port) throws InterruptedException {
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Bắt đầu khởi động server tại port: " + port);
            while (true) { 
                Socket socket = serverSocket.accept();
                System.out.println("Chào mừng người dùng đã kết nối đến server!");
                Client client = new Client(socket, this);
                clients.add(client);
                Thread clientThread = new Thread(client);
                clientThread.start();
                clientThreads.add(clientThread);
            }
        } catch (IOException e) {
            for (Thread clientThread: this.clientThreads) {
                clientThread.join();
            }
        }
    }

    public void removeClient(Client removingClient) {
        for (Client client: clients) {
            if (client == removingClient) {
                clients.remove(client);
            }
        }
    }

    public  void notify(String message, Client currentClient) {
       
        for (Client client: clients) {
            try {
                if (client != currentClient) {
                    client.sendMessage(message);
                }
            } catch(IOException e) {
                System.err.print("Lỗi khi gởi dữ liệu qua client" + client.getSocket().getLocalAddress() + ": "+ e);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server sever = Server.getInstance();
        sever.startServer(6789);
    }
}

class Client implements Runnable {

    private final Socket clientSocket;
    private final Server server;

    public Client(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
    }

    public Socket getSocket() {
        return this.clientSocket;
    }

    public void sendMessage(String message) throws IOException {
        OutputStream output = this.clientSocket.getOutputStream();
        output.write((message + '\n').getBytes());
    }

    @Override
    public void run() {
        System.out.println("Bắt đầu lắng nghe người dùng");
        try (BufferedReader message = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = message.readLine()) != null) {
                if (line != null) {
                    System.out.println("Gởi tin nhắn về cho người dùng");
                    this.server.notify(line, this);
                }
            }
        } catch (IOException e) {
            System.out.println("Socket đã bị đóng");
            this.server.removeClient(this);
        }
    }
}