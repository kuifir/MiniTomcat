package com.kuifir.mini.connector.http;

import com.kuifir.mini.*;
import com.kuifir.mini.session.StandardSessionFacade;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HttpRequestImpl implements HttpServletRequest,Request {
    private InputStream input;
    private SocketInputStream sis;
    private String uri;
    // url参数字符串
    private String queryString;
    InetAddress address;
    int port;
    private boolean parsed = false;
    protected HashMap<String, String> headers = new HashMap<>();
    // 请求体参数信息
    protected Map<String, String[]> parameters = new ConcurrentHashMap<>();
    HttpRequestLine requestLine = new HttpRequestLine();
    Cookie[] cookies;
    HttpSession session;
    String sessionid;
    StandardSessionFacade sessionFacade;
    private HttpResponseImpl response;
    String docbase;
    public HttpRequestImpl() {
    }

    public HttpRequestImpl(InputStream input) {
        this.input = input;
        this.sis = new SocketInputStream(this.input, 2048);
    }

    @Override
    public Connector getConnector() {
        return null;
    }

    @Override
    public void setConnector(Connector connector) {

    }

    @Override
    public Context getContext() {
        return null;
    }

    @Override
    public void setContext(Context context) {

    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    public ServletRequest getRequest() {
        return this;
    }

    @Override
    public Response getResponse() {
        return null;
    }

    @Override
    public void setResponse(Response response) {

    }

    @Override
    public Socket getSocket() {
        return null;
    }

    @Override
    public void setSocket(Socket socket) {

    }

    @Override
    public InputStream getStream() {
        return null;
    }

    public void setStream(InputStream input) {
        this.input = input;
        this.sis = new SocketInputStream(this.input, 2048);
    }

    @Override
    public Wrapper getWrapper() {
        return null;
    }

    @Override
    public void setWrapper(Wrapper wrapper) {

    }

    @Override
    public ServletInputStream createInputStream() throws IOException {
        return null;
    }

    @Override
    public void finishRequest() throws IOException {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void setContentLength(int length) {

    }

    @Override
    public void setContentType(String type) {

    }

    @Override
    public void setProtocol(String protocol) {

    }

    @Override
    public void setRemoteAddr(String remote) {

    }

    @Override
    public void setScheme(String scheme) {

    }

    @Override
    public void setServerPort(int port) {

    }

    public void setResponse(HttpResponseImpl response) {
        this.response = response;
    }

    public void parse(Socket socket) {
        try {
            parseConnection(socket);
            this.sis.readRequestLine(requestLine);
            // url中可能包含参数信息，应该将参数和地址分离
            // this.uri = new String(requestLine.uri, 0, requestLine.uriEnd);
            parseRequestLine();
            parseHeaders();
        } catch (IOException | ServletException e) {
            e.printStackTrace();
        }
    }

    //处理请求行
    public void parseRequestLine() {
        //以问号判断是否带有参数串
        int question = requestLine.indexOf("?");
        String tmp = ";" + DefaultHeaders.JSESSIONID_NAME + "=";
        if (question >= 0) {
            queryString = new String(requestLine.uri, question + 1, requestLine.uriEnd - question - 1);
            uri = new String(requestLine.uri, 0, question);
            //处理参数串中带有jsessionid的情况
            int semicolon = uri.indexOf(tmp);
            if (semicolon >= 0) {
                sessionid = uri.substring(semicolon + tmp.length());
                uri = uri.substring(0, semicolon);
            }
        } else {
            queryString = null;
            this.uri = new String(requestLine.uri, 0, requestLine.uriEnd);
            int semicolon = uri.indexOf(tmp);
            if (semicolon >= 0) {
                sessionid = uri.substring(semicolon + tmp.length());
                uri = uri.substring(0, semicolon);
            }
        }
        //get context from uri
        int contextslash = uri.indexOf("/", 1);
        if (contextslash != -1) {
            this.docbase = uri.substring(1, contextslash);
            uri = uri.substring(contextslash);
        }
    }

    private void parseConnection(Socket socket) {
        address = socket.getInetAddress();
        port = socket.getPort();
    }

    //解析所有header信息
    private void parseHeaders() throws IOException, ServletException {
        while (true) {
            HttpHeader header = new HttpHeader();
            sis.readHeader(header);
            if (header.nameEnd == 0) {
                if (header.valueEnd == 0) {
                    return;
                } else {
                    throw new ServletException("httpProcessor.parseHeaders.colon");
                }
            }
            String name = new String(header.name, 0, header.nameEnd);
            name = name.toLowerCase();
            String value = new String(header.value, 0, header.valueEnd);
            // Set the corresponding request headers
            switch (name) {
                case DefaultHeaders.ACCEPT_LANGUAGE_NAME -> headers.put(name, value);
                case DefaultHeaders.CONTENT_LENGTH_NAME -> headers.put(name, value);
                case DefaultHeaders.CONTENT_TYPE_NAME -> headers.put(name, value);
                case DefaultHeaders.HOST_NAME -> headers.put(name, value);
                case DefaultHeaders.TRANSFER_ENCODING_NAME -> headers.put(name, value);
                case DefaultHeaders.COOKIE_NAME -> {
                    headers.put(name, value);
                    //处理cookie和session
                    this.cookies = parseCookieHeader(value);
                    for (Cookie cookie : cookies) {
                        if (cookie.getName().equals("jsessionid")) {
                            this.sessionid = cookie.getValue();
                        }
                    }
                }
                case DefaultHeaders.CONNECTION_NAME -> {
                    headers.put(name, value);
                    if (value.equals("close")) {
                        response.setHeader("Connection", "close");
                    }
                }
                default -> headers.put(name, value);
            }
        }
    }

    //解析Cookie头，格式为: key1=value1;key2=value2
    public Cookie[] parseCookieHeader(String header) {
        if ((header == null) || (header.isEmpty())) return (new Cookie[0]);
        ArrayList<Cookie> cookieal = new ArrayList<>();
        while (!header.isEmpty()) {
            int semicolon = header.indexOf(';');
            if (semicolon < 0) semicolon = header.length();
            if (semicolon == 0) break;
            String token = header.substring(0, semicolon);
            if (semicolon < header.length()) header = header.substring(semicolon + 1);
            else header = "";
            try {
                int equals = token.indexOf('=');
                if (equals > 0) {
                    String name = token.substring(0, equals).trim();
                    String value = token.substring(equals + 1).trim();
                    cookieal.add(new Cookie(name, value));
                }
            } catch (Throwable ignored) {
            }
        }
        return cookieal.toArray(new Cookie[0]);
    }

    protected void parseParameters() {
        //设置字符集
        String encoding = getCharacterEncoding();
        if (encoding == null) {
            encoding = "ISO-8859-1";
        }
        //获取查询串
        String qString = getQueryString();
        if (qString != null) {
            byte[] bytes = new byte[qString.length()];
            try {
                bytes = qString.getBytes(encoding);
                parseParameters(this.parameters, bytes, encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //获取 content Type
        String contentType = getContentType();
        if (contentType == null)
            contentType = "";
        int semicolon = contentType.indexOf(';');
        if (semicolon >= 0) {
            contentType = contentType.substring(0, semicolon).trim();
        } else {
            contentType = contentType.trim();
        }
        //对POST方法，从body中解析参数
        if ("POST".equals(getMethod()) && (getContentLength() > 0)
                && "application/x-www-form-urlencoded".equals(contentType)) {
            try {
                int max = getContentLength();
                int len = 0;
                byte[] buf = new byte[getContentLength()];
                ServletInputStream is = getInputStream();
                while (len < max) {
                    int next = is.read(buf, len, max - len);
                    if (next < 0) {
                        break;
                    }
                    len += next;
                }
//                is.close();
                if (len < max) {
                    throw new RuntimeException("Content length mismatch");
                }
                parseParameters(this.parameters, buf, encoding);
            } catch (UnsupportedEncodingException ignored) {
            } catch (IOException e) {
                throw new RuntimeException("Content read fail");
            }
        }
    }

    //十六进制字符到数字的转换
    private byte convertHexDigit(byte b) {
        if ((b >= '0') && (b <= '9')) return (byte) (b - '0');
        if ((b >= 'a') && (b <= 'f')) return (byte) (b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F')) return (byte) (b - 'A' + 10);
        return 0;
    }

    public void parseParameters(Map<String, String[]> map, byte[] data, String encoding)
            throws UnsupportedEncodingException {
        if (parsed)
            return;
        if (data != null && data.length > 0) {
            int ix = 0;
            int ox = 0;
            String key = null;
            String value = null;
            //解析参数串，处理特殊字符
            while (ix < data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                    case '&':   //两个参数之间的分隔符，遇到这个字符保存已经解析的key和value
                        value = new String(data, 0, ox, encoding);
                        if (key != null) {
                            putMapEntry(map, key, value);
                            key = null;
                        }
                        ox = 0;
                        break;
                    case '=': //参数的key/value的分隔符
                        key = new String(data, 0, ox, encoding);
                        ox = 0;
                        break;
                    case '+': //特殊字符，空格
                        data[ox++] = (byte) ' ';
                        break;
                    case '%': //处理%NN表示的ASCII字符
                        data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4)
                                + convertHexDigit(data[ix++]));
                        break;
                    default:
                        data[ox++] = c;
                }
            }
            //最后一个参数没有&结尾
            //The last value does not end in '&'.  So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }
        parsed = true;
    }

    //给key设置新值，多值用数组来存储
    private static void putMapEntry(Map<String, String[]> map, String name, String value) {
        String[] newValues = null;
        String[] oldValues = map.get(name);
        if (oldValues == null) {
            newValues = new String[1];
            newValues[0] = value;
        } else {
            newValues = new String[oldValues.length + 1];
            System.arraycopy(oldValues, 0, newValues, 0, oldValues.length);
            newValues[oldValues.length] = value;
        }
        map.put(name, newValues);
    }

    public String getUri() {
        return this.uri;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return null;
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return new String(this.requestLine.method, 0, this.requestLine.methodEnd);
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        return null;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return null;
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return null;
    }

    @Override
    //如果有存在的session，直接返回，如果没有，创建一个新的session
    public HttpSession getSession(boolean create) {
        if (sessionFacade != null) return sessionFacade;
        if (sessionid != null) {
            session = HttpConnector.sessions.get(sessionid);
            if (session != null) {
                sessionFacade = new StandardSessionFacade(session);
                return sessionFacade;
            } else {
                session = HttpConnector.createSession();
                sessionFacade = new StandardSessionFacade(session);
                HttpConnector.sessions.put(sessionid, session);
                return sessionFacade;
            }
        } else {
            session = HttpConnector.createSession();
            sessionFacade = new StandardSessionFacade(session);
            sessionid = session.getId();
            return sessionFacade;
        }
    }

    public String getSessionId() {
        return this.sessionid;
    }

    @Override
    public HttpSession getSession() {
        return this.sessionFacade;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return null;
    }

    @Override
    public String getCharacterEncoding() {
        return headers.get(DefaultHeaders.TRANSFER_ENCODING_NAME);
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return Integer.parseInt(headers.get(DefaultHeaders.CONTENT_LENGTH_NAME));
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return headers.get(DefaultHeaders.CONTENT_TYPE_NAME);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return sis;
    }

    @Override
    public String getParameter(String name) {
        parseParameters();
        String[] values = parameters.get(name);
        if (values != null)
            return (values[0]);
        else
            return (null);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        parseParameters();
        return (this.parameters);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        parseParameters();
        return (Collections.enumeration(parameters.keySet()));
    }

    @Override
    public String[] getParameterValues(String name) {
        parseParameters();
        return parameters.get(name);
    }

    @Override
    public String getProtocol() {
        return null;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return null;
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws
            IllegalStateException {
        return null;
    }

    public String getDocbase() {
        return docbase;
    }

    public void setDocbase(String docbase) {
        this.docbase = docbase;
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return null;
    }
}
