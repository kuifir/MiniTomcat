package com.kuifir.mini.startup;

import com.kuifir.mini.connector.http.HttpConnector;
import com.kuifir.mini.core.StandardContext;

import java.io.File;

public class Bootstrap {
    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";

    public static void main(String[] args) {
        //创建connector和container
        HttpConnector connector = new HttpConnector();
        StandardContext container = new StandardContext();
        connector.setContainer(container);
        container.setConnector(connector);
        connector.start();
    }
}
