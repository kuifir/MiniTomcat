package com.kuifir.mini.logger;

import com.kuifir.mini.util.StringManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

public class FileLogger extends LoggerBase{
    private String date = "";
    private String directory = "logs";
    protected static final String info = "com.kuifir.mini.logger.FileLogger/0.1";
    private String prefix = "mini.";
    private StringManager sm = StringManager.getManager(Constants.Package);
    private boolean started = false;
    private String suffix = ".log";
    private boolean timestamp = true;
    private PrintWriter writer = null;
    public String getDirectory() {
        return (directory);
    }
    public void setDirectory(String directory) {
        String oldDirectory = this.directory;
        this.directory = directory;
    }
    public String getPrefix() {
        return (prefix);
    }
    public void setPrefix(String prefix) {
        String oldPrefix = this.prefix;
        this.prefix = prefix;
    }
    public String getSuffix() {
        return (suffix);
    }
    public void setSuffix(String suffix) {
        String oldSuffix = this.suffix;
        this.suffix = suffix;
    }
    public boolean getTimestamp() {
        return (timestamp);
    }
    public void setTimestamp(boolean timestamp) {
        boolean oldTimestamp = this.timestamp;
        this.timestamp = timestamp;
    }
    public void log(String msg) {
        // 当前时间Construct the timestamp we will use, if requested
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        String tsString = ts.toString().substring(0, 19);
        String tsDate = tsString.substring(0, 10);
        // 如果日期变化了，新生成一个log文件
        // If the date has changed, switch log files
        if (!date.equals(tsDate)) {
            synchronized (this) {
                if (!date.equals(tsDate)) {
                    close();
                    date = tsDate;
                    open();
                }
            }
        }
        // 记录日志，带上时间戳
        if (writer != null) {
            if (timestamp) {
                writer.println(tsString + " " + msg);
            } else {
                writer.println(msg);
            }
        }
    }
    private void close() {
        if (writer == null)
            return;
        writer.flush();
        writer.close();
        writer = null;
        date = "";
    }
    private void open() {
        File dir = new File(directory);
        if (!dir.isAbsolute())
            dir = new File(System.getProperty("catalina.base"), directory);
        dir.mkdirs();
        // 打开日志文件 Open the current log file
        try {
            String pathname = dir.getAbsolutePath() + File.separator +
                    prefix + date + suffix;
            writer = new PrintWriter(new FileWriter(pathname, true), true);
        } catch (IOException e) {
            writer = null;
        }
    }
}
