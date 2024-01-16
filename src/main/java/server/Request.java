package server;

import java.io.IOException;
import java.io.InputStream;

public class Request {
    private InputStream input;
    private String uri;

    public Request(InputStream input) {
        this.input = input;
    }

    public void parse() {
        StringBuffer request = new StringBuffer(2048);
        int i;
        byte[] buffer = new byte[2048];
        try {
            i = input.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            i = -1;
        }
        for (int j = 0; j < i; j++) {
            request.append((char) buffer[j]);
        }
        uri = parseUri(request.toString());
    }

    private String parseUri(String requestString) {
        int indext1, indext2;
        indext1 = requestString.indexOf(' ');
        if (indext1 != -1) {
            indext2 = requestString.indexOf(' ', indext1 + 1);
            if (indext2 > indext1) {
                return requestString.substring(indext1 + 1, indext2);
            }
        }
        return null;
    }

    public String getUri() {
        return uri;
    }
}
