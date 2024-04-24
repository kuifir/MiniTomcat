package com.kuifir.mini.valves;

import com.kuifir.mini.Container;
import com.kuifir.mini.Valve;

public abstract class ValveBase implements Valve {
    protected Container container = null;
    protected int debug = 0;
    protected static String info = "com.kuifir.mini.valves.ValveBase/0.1";

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

    public int getDebug() {
        return debug;
    }

    public void setDebug(int debug) {
        this.debug = debug;
    }

}
