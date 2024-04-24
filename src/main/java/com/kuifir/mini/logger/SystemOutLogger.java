package com.kuifir.mini.logger;

/**
 * 标准输出日志类
 */
public class SystemOutLogger extends LoggerBase {
    protected static final String info = "com.kuifir.mini.logger.SystemOutLogger/1.0";
    public void log(String msg) {
        System.out.println(msg);
    }
}
