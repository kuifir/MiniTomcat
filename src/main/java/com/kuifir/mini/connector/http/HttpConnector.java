package com.kuifir.mini.connector.http;

import com.kuifir.mini.*;
import com.kuifir.mini.core.StandardContext;
import com.kuifir.mini.session.StandardSession;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class HttpConnector implements Connector, Runnable {
    int minProcessors = 3;
    int maxProcessors = 10;
    int curProcessors = 0;
    // 存放多个processor的池子
    final Deque<HttpProcessor> processors = new ArrayDeque<>();
    //sessions map存放session
    public static Map<String, HttpSession> sessions = new ConcurrentHashMap<>();
    //这是与connector相关联的container
    Container container = null;
    private String info = "com.mini.connector.http.HttpConnector/0.1";
    private int port = 8080;
    private String threadName = null;

    //创建新的session
    public static StandardSession createSession() {
        StandardSession session = new StandardSession();
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        String sessionId = generateSessionId();
        session.setId(sessionId);
        sessions.put(sessionId, session);
        return (session);
    }

    //以随机方式生成byte数组,形成sessionid
    protected static synchronized String generateSessionId() {
        Random random = new Random();
        long seed = System.currentTimeMillis();
        random.setSeed(seed);
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            byte b1 = (byte) ((aByte & 0xf0) >> 4);
            byte b2 = (byte) (aByte & 0x0f);
            if (b1 < 10) result.append((char) ('0' + b1));
            else result.append((char) ('A' + (b1 - 10)));
            if (b2 < 10) result.append((char) ('0' + b2));
            else result.append((char) ('A' + (b2 - 10)));
        }
        return (result.toString());
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // initialize processors pool
        for (int i = 0; i < minProcessors; i++) {
            HttpProcessor initProcessor = new HttpProcessor(this);
            initProcessor.start();
            processors.push(initProcessor);
            curProcessors++;
        }
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                //得到一个新的processor，这个processor从池中获取(池中有可能新建)
                HttpProcessor processor = createProcessor();
                if (processor == null) {
                    socket.close();
                    continue;
                }
                // 分配给这个processor
                processor.assign(socket);
                // 解耦后关闭操作自己执行
                // Close the socket
//                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        threadName = "HttpConnector[" + port + "]";
        log("httpConnector.starting " + threadName);
        Thread thread = new Thread(this);
        thread.start();
    }

    //从池子中获取一个processor，如果池子为空且小于最大限制，则新建一个
    private HttpProcessor createProcessor() {
        synchronized (processors) {
            if (!processors.isEmpty()) {
                // 获取一个
                return processors.pop();
            }
            if (curProcessors < maxProcessors) {
                // 新建一个
                return newProcessor();
            } else {
                return null;
            }
        }
    }

    // 新建一个processor
    private HttpProcessor newProcessor() {
        HttpProcessor initProcessor = new HttpProcessor(this);
        initProcessor.start();
        processors.push(initProcessor);
        curProcessors++;
        log("newProcessor");
        return processors.pop();
    }

    //记录日志
    private void log(String message) {
        Logger logger = container.getLogger();
        String localName = threadName;
        if (localName == null) localName = "HttpConnector";
        if (logger != null) {
            logger.log(localName + " " + message);
        } else {
            System.out.println(localName + " " + message);
        }
    }

    //记录日志
    private void log(String message, Throwable throwable) {
        Logger logger = container.getLogger();
        String localName = threadName;
        if (localName == null) localName = "HttpConnector";
        if (logger != null) logger.log(localName + " " + message, throwable);
        else {
            System.out.println(localName + " " + message);
            throwable.printStackTrace(System.out);
        }
    }

    void recycle(HttpProcessor processor) {
        processors.push(processor);
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public String getInfo() {
        return this.info;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public Request createRequest() {
        return null;
    }

    @Override
    public Response createResponse() {
        return null;
    }

    @Override
    public void initialize() {

    }

}
