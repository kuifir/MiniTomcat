package com.kuifir.mini.logger;

/**
 * 标准错误日志类：
 */
public class SystemErrLogger extends LoggerBase {
    protected static final String info = "com.kuifir.mini.logger.SystemErrLogger/0.1";

    public void log(String msg) {
        System.err.println(msg);
    }
}