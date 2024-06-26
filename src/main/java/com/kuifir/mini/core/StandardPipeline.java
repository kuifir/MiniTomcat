package com.kuifir.mini.core;

import com.kuifir.mini.*;

import javax.servlet.ServletException;
import java.io.IOException;

public class StandardPipeline implements Pipeline {
    protected Valve basic = null;
    protected Container container = null;
    protected int debug = 0;
    protected String info = "com.kuifir.mini.core.StandardPipeline/0.1";

    protected Valve[] valves = new Valve[0];

    public StandardPipeline() {
        this(null);
    }

    public StandardPipeline(Container container) {
        super();
        setContainer(container);
    }

    public void setContainer(Container container) {
        this.container = container;
    }


    public Valve getBasic() {
        return (this.basic);
    }

    public void setBasic(Valve valve) {
        // Change components if necessary
        Valve oldBasic = this.basic;
        if (oldBasic == valve)
            return;
        // Start the new component if necessary
        if (valve == null)
            return;
        valve.setContainer(container);
        this.basic = valve;
    }

    //添加valve
    public void addValve(Valve valve) {
        // Add this Valve to the set associated with this Pipeline
        synchronized (valves) {
            Valve results[] = new Valve[valves.length + 1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            valve.setContainer(container);
            results[valves.length] = valve;
            valves = results;
        }
    }

    public Valve[] getValves() {
        if (basic == null)
            return (valves);
        synchronized (valves) {
            Valve[] results = new Valve[valves.length + 1];
            System.arraycopy(valves, 0, results, 0, valves.length);
            results[valves.length] = basic;
            return (results);
        }
    }

    //核心方法invoke
    public void invoke(Request request, Response response)
            throws IOException, ServletException {
        System.out.println("StandardPipeline invoke()");
        // 转而调用context中的invoke，发起职责链调用
        // Invoke the first Valve in this pipeline for this request
        (new StandardPipelineValveContext()).invokeNext(request, response);
    }

    public void removeValve(Valve valve) {
        synchronized (valves) {
            // Locate this Valve in our list
            int j = -1;
            for (int i = 0; i < valves.length; i++) {
                if (valve == valves[i]) {
                    j = i;
                    break;
                }
            }
            if (j < 0)
                return;
            valve.setContainer(null);
            // Remove this valve from our list
            Valve results[] = new Valve[valves.length - 1];
            int n = 0;
            for (int i = 0; i < valves.length; i++) {
                if (i == j)
                    continue;
                results[n++] = valves[i];
            }
            valves = results;
        }
    }

    //内部类，维护了stage，表示valves数组中的位置，逐个invoke
    protected class StandardPipelineValveContext implements ValveContext {
        protected int stage = 0;

        @Override
        public String getInfo() {
            return info;
        }

        public void invokeNext(Request request, Response response)
                throws IOException, ServletException {
            System.out.println("StandardPipelineValveContext invokeNext()");
            int subscript = stage;
            stage = stage + 1;
            // Invoke the requested Valve for the current request thread
            if (subscript < valves.length) {
                valves[subscript].invoke(request, response, this);
            } else if ((subscript == valves.length) && (basic != null)) {
                basic.invoke(request, response, this);
            } else {
                throw new ServletException("standardPipeline.noValve");
            }
        }
    }
}
