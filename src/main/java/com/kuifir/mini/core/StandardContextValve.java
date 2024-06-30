package com.kuifir.mini.core;

import com.kuifir.mini.Request;
import com.kuifir.mini.Response;
import com.kuifir.mini.ValveContext;
import com.kuifir.mini.connector.http.HttpRequestImpl;
import com.kuifir.mini.valves.ValveBase;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

final class StandardContextValve extends ValveBase {
    private StandardContext standardContext;
    private static final String info =
            "com.kuifir.mini.core.StandardContextValve/1.0";

    public String getInfo() {
        return (info);
    }

    public StandardContextValve(StandardContext standardContext) {
        this.standardContext = standardContext;
    }

    public void invoke(Request request, Response response, ValveContext valveContext) throws IOException, ServletException {
        System.out.println("StandardContextValve invoke()");
        StandardWrapper servletWrapper = null;
        String uri = ((HttpRequestImpl) request).getUri();
        //通过uri拿到pattern
        String servletPattern = uri.substring(uri.lastIndexOf("/"));
        //通过pattern找到合适的servlet名
        String servletName = this.urlMatch(servletPattern);
        StandardContext context = (StandardContext) getContainer();
        servletWrapper = (StandardWrapper) context.getWrapper(servletName);
        try {
            System.out.println("Call service()");
            servletWrapper.invoke(request, response);
        } catch (Exception e) {
            System.out.println(e.toString());
        } catch (Throwable e) {
            System.out.println(e.toString());
        }
    }

    //简单的匹配规则，以url-pattern开头继任为匹配上
    private String urlMatch(String urlPattern) {
        Map<String,String> servletMappingMap = standardContext.getServletMappingMap();
        Set<String> keySet = servletMappingMap.keySet();
        for (Map.Entry<String,String> entry : servletMappingMap.entrySet()) {
            String key = entry.getKey();
            if (urlPattern.startsWith(key)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
