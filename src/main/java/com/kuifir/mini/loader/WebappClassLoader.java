package com.kuifir.mini.loader;

import java.net.URL;
import java.net.URLClassLoader;

public class WebappClassLoader extends URLClassLoader {
    protected boolean delegate = false;
    private ClassLoader parent = null;
    private ClassLoader system = null;

    public WebappClassLoader() {
        super(new URL[0]);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public WebappClassLoader(URL[] urls) {
        super(urls);
        this.parent = getParent();
        system = getSystemClassLoader();
    }

    public WebappClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    public WebappClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.parent = parent;
        system = getSystemClassLoader();
    }

    public boolean getDelegate() {
        return (this.delegate);
    }

    public void setDelegate(boolean delegate) {
        this.delegate = delegate;
    }

    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        clazz = super.findClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        // Return the class we have located
        return (clazz);
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return (loadClass(name, false));
    }

    //核心方法，按照自定义的加载次序加载类
    public Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        Class<?> clazz = null;
        try {
            //首先是用系统类加载器加载类
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
        }

        boolean delegateLoad = delegate;
        //到了这里，系统类加载器加载不成功，则判断是否为双亲委托模式，如果是，
        //则用parent来加载器来加载
        if (delegateLoad) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }
        //到了这里，或者是父类加载器加载不成功，或者是不支持双亲委托模式，
        //所以要自己去加载类
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            ;
        }
        //到这里，自己加载不成功，则反过来交给父类加载器去加载
        if (!delegateLoad) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }
        throw new ClassNotFoundException(name);
    }

    private void log(String message) {
        System.out.println("WebappClassLoader: " + message);
    }

    private void log(String message, Throwable throwable) {
        System.out.println("WebappClassLoader: " + message);
        throwable.printStackTrace(System.out);
    }

}