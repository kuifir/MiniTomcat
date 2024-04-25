package com.kuifir.mini.startup;

import com.kuifir.mini.Logger;
import com.kuifir.mini.connector.http.HttpConnector;
import com.kuifir.mini.core.FilterDef;
import com.kuifir.mini.core.FilterMap;
import com.kuifir.mini.core.StandardContext;
import com.kuifir.mini.logger.FileLogger;

import java.io.File;

public class Bootstrap {
    public static final String WEB_ROOT = System.getProperty("user.dir") + File.separator + "webroot";
    private static int debug = 0;

    public static void main(String[] args) {
        if (debug >= 1) {
            log(".... startup ....");
        }
        //创建connector和container
        HttpConnector connector = new HttpConnector();
        StandardContext container = new StandardContext();
        connector.setContainer(container);
        container.setConnector(connector);
        // 添加日志组件
        Logger logger = new FileLogger();
        container.setLogger(logger);
        container.setLogger(logger);
        // 添加过滤器
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("TestFilter");
        filterDef.setFilterClass("test.TestFilter");
        container.addFilterDef(filterDef);
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("TestFilter");
        filterMap.setURLPattern("/*");
        container.addFilterMap(filterMap);
        container.filterStart();

        connector.start();
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
