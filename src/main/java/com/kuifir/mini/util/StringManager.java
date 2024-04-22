package com.kuifir.mini.util;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StringManager {
    private StringManager(String packageName) {
    }
    public String getString(String key) {
        if (key == null) {
            String msg = "key is null";
            throw new NullPointerException(msg);
        }
        String str = null;
        str = key;
        return str;
    }
    //用参数拼串
    public String getString(String key, Object[] args) {
        String iString = null;
        String value = getString(key);
        try {
            //消除null对象
            Object[] nonNullArgs = args;
            for (int i=0; i<args.length; i++) {
                if (args[i] == null) {
                    if (nonNullArgs==args) {
                        nonNullArgs= args.clone();
                    }
                    nonNullArgs[i] = "null";
                }
            }
            //拼串
            iString = MessageFormat.format(value, nonNullArgs);
        } catch (IllegalArgumentException iae) {
            StringBuilder buf = new StringBuilder();
            buf.append(value);
            for (int i = 0; i < args.length; i++) {
                buf.append(" arg[").append(i).append("]=").append(args[i]);
            }
            iString = buf.toString();
        }
        return iString;
    }
    public String getString(String key, Object arg) {
        Object[] args = new Object[] {arg};
        return getString(key, args);
    }
    public String getString(String key, Object arg1, Object arg2) {
        Object[] args = new Object[] {arg1, arg2};
        return getString(key, args);
    }
    public String getString(String key, Object arg1, Object arg2,
                            Object arg3) {
        Object[] args = new Object[] {arg1, arg2, arg3};
        return getString(key, args);
    }
    public String getString(String key, Object arg1, Object arg2,
                            Object arg3, Object arg4) {
        Object[] args = new Object[] {arg1, arg2, arg3, arg4};
        return getString(key, args);
    }
    private static Map<String,StringManager> managers = new ConcurrentHashMap<>();
    //每个package有相应的StringManager
    public synchronized static StringManager getManager(String packageName) {
        return managers.computeIfAbsent(packageName, StringManager::new);
    }
}