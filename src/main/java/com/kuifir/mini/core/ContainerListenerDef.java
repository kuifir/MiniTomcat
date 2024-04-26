package com.kuifir.mini.core;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ContainerListenerDef {
    private String description = null;

    private String displayName = null;

    private String listenerClass = null;

    private Map<String, String> parameters = new ConcurrentHashMap<>();
    public String getDescription() {
        return (this.description);
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getDisplayName() {
        return (this.displayName);
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }


    public String getListenerClass() {
        return (this.listenerClass);
    }

    public void setListenerClass(String listenerClass) {
        this.listenerClass = listenerClass;
    }

    private String listenerName = null;

    public String getListenerName() {
        return (this.listenerName);
    }

    public void setListenerName(String listenerName) {
        this.listenerName = listenerName;
    }


    public Map<String, String> getParameterMap() {
        return (this.parameters);
    }

    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }

    public String toString() {
        String sb = "ListenerDef[" + "listenerName=" +
                this.listenerName +
                ", listenerClass=" +
                this.listenerClass +
                "]";
        return (sb);
    }
}
