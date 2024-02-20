package server;

import server.processor.ServletProcessor;
import server.processor.StaticResourceProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class HttpProcessor {
    public HttpProcessor() {
    }
    public void process(Socket socket){
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
//            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
