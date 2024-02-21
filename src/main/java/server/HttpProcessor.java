package server;

import server.processor.ServletProcessor;
import server.processor.StaticResourceProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor implements Runnable {
    Socket socket;
    boolean available = false;
    HttpConnector connector;

    public HttpProcessor(HttpConnector connector) {
        this.connector = connector;
    }

    @Override
    public void run() {
        while (true) {
            // 等待socket分配过来
            Socket socket = await();
            if (socket == null) {
                continue;
            }
            // 处理请求
            process(socket);
            // 回收processor
            connector.recycle(this);
        }
    }

    synchronized void assign(Socket socket) {
        // 等待connector提供一个新的socket
        while (available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        // 获取到这个新的Socket
        this.socket = socket;
        // 把标志设置回去
        available = true;
        //通知另外的线程
        notifyAll();
    }

    private synchronized Socket await() {
        // 等待connector提供一个新的socket
        while (!available) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        // 获得这个新的Socket
        Socket socket = this.socket;
        //设置标志为false
        available = false;
        //通知另外的线程
        notifyAll();
        return (socket);
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.start();
    }

    public void process(Socket socket) {
        try {
            Thread.sleep(3000);
            System.out.println(System.currentTimeMillis());
            System.out.println(Thread.currentThread().getName());
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        InputStream input;
        OutputStream output;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            // create Request object and parse
            Request request = new Request(input);
            request.parse();
            // create Response object
            Response response = new Response(output);
            response.setRequest(request);

            // check if this is a request for a servlet or a static resource
            // a request for a servlet begins with "/servlet/"
            if (request.getUri().startsWith("/servlet/")) {
                ServletProcessor processor = new ServletProcessor();
                processor.process(request, response);
            } else {
                StaticResourceProcessor processor = new StaticResourceProcessor();
                processor.process(request, response);
            }
            // close the socket
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
