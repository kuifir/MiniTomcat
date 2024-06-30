package com.kuifir.mini.connector.http;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

public class HttpProcessor implements Runnable {
    private Socket socket;
    private boolean available = false;
    private HttpConnector connector;
    private int serverPort = 0;
    private boolean keepAlive = false;
    private boolean http11 = true;

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
//        try {
//            Thread.sleep(3000);
//            System.out.println(System.currentTimeMillis());
//            System.out.println(Thread.currentThread().getName());
//        } catch (InterruptedException e1) {
//            e1.printStackTrace();
//        }
        InputStream input;
        OutputStream output;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
            keepAlive = true;
            // 添加keepAlive处理
            while (keepAlive) {
                // create Request object and parse
                HttpRequestImpl request = new HttpRequestImpl(input);
                // create Response object
                HttpResponseImpl response = new HttpResponseImpl(output);
                response.setRequest(request);
                request.setResponse(response);

                request.parse(socket);
                //handle session
                if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
                    request.getSession(true);
                }

                try {
                    response.sendHeaders();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                // check if this is a request for a servlet or a static resource
                // a request for a servlet begins with "/servlet/"
                if (request.getUri().startsWith("/resources/")) {
                    StaticResourceProcessor processor = new StaticResourceProcessor();
                    processor.process(request, response);
                } else {
                    ServletProcessor processor = new ServletProcessor(this.connector);
                    processor.process(request, response);
                }
                finishResponse(response);
                System.out.println("response header connection------" + response.getHeader("Connection"));
                if (Objects.isNull(response.getHeader("Connection")) || "close".equals(response.getHeader("Connection"))) {
                    keepAlive = false;
                }
            }

            // close the socket
            socket.close();
            socket = null;
        } catch (IOException | ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private void finishResponse(HttpResponseImpl response) {
        response.finishResponse();
    }
}
