package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpConnector implements Runnable {
    @Override
    public void run() {
        ServerSocket serverSocket;
        int port = 8080;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                HttpProcessor processor = new HttpProcessor();
                processor.process(socket);
                // Close the socket
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }
}
