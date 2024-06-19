package com.kuifir.mini.startup;

import com.kuifir.mini.Loader;
import com.kuifir.mini.connector.http.HttpConnector;
import com.kuifir.mini.core.*;
import com.kuifir.mini.loader.CommonLoader;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

public class BootStrap {
    public static final String MINIT_HOME = System.getProperty("user.dir");
    public static String WEB_ROOT = System.getProperty("user.dir");
    private static int debug = 0;
    public static int PORT = 8080;
    public static String HOST="localhost";

    public static void main(String[] args) {
        if (debug >= 1) {
            log(".... startup ....");
        }
        // scan server.xml
        // scan web.xml
        String file = MINIT_HOME + File.separator + "conf" + File.separator + "server.xml";
        SAXReader reader = new SAXReader();
        Document document;
        try {
            document = reader.read(file);
            Element root = document.getRootElement();
            Element connectorelement = root.element("Connector");
            Attribute portattribute = connectorelement.attribute("port");
            PORT = Integer.parseInt(portattribute.getText());
            Element hostelement = root.element("Host");
            Attribute appbaseattribute = hostelement.attribute("appBase");
            WEB_ROOT = WEB_ROOT + File.separator + appbaseattribute.getText();
            HOST = hostelement.attribute("name").getText();
        } catch (Exception e) {
            log(e.getMessage());
        }
        log(MINIT_HOME);
        log(WEB_ROOT);
        System.setProperty("minit.home", MINIT_HOME);
        System.setProperty("minit.base", WEB_ROOT);

        HttpConnector connector = new HttpConnector();
        StandardHost container = new StandardHost();
        Loader loader = new CommonLoader();
        container.setLoader(loader);
        loader.start();
        connector.setContainer(container);
        container.setConnector(connector);
        container.start();
        connector.start();
        // 多应用支持前
//        //创建connector和container
//        HttpConnector connector = new HttpConnector();
//        StandardContext container = new StandardContext();
//        connector.setContainer(container);
//        container.setConnector(connector);
//        // 添加日志组件
//        Logger logger = new FileLogger();
//        container.setLogger(logger);
//        container.setLogger(logger);
//        // 添加过滤器
//        FilterDef filterDef = new FilterDef();
//        filterDef.setFilterName("TestFilter");
//        filterDef.setFilterClass("test.TestFilter");
//        container.addFilterDef(filterDef);
//        FilterMap filterMap = new FilterMap();
//        filterMap.setFilterName("TestFilter");
//        filterMap.setURLPattern("/*");
//        container.addFilterMap(filterMap);
//        container.filterStart();
//        ContainerListenerDef listenerDef = new ContainerListenerDef();
//        listenerDef.setListenerName("TestListener");
//        listenerDef.setListenerClass("test.TestListener");
//        container.addListenerDef(listenerDef);
//        container.listenerStart();
//        container.start();
//        connector.start();
    }

    private static void log(String message) {
        System.out.print("Bootstrap: ");
        System.out.println(message);
    }

    private static void log(String message, Throwable exception) {
        log(message);
        exception.printStackTrace(System.out);
    }
}
