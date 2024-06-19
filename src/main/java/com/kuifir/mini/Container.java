package com.kuifir.mini;

import javax.servlet.ServletException;
import java.io.IOException;

public interface Container {
    static final String ADD_CHILD_EVENT = "addChild";
    static final String REMOVE_CHILD_EVENT = "removeChild";

    String getInfo();

    Loader getLoader();

    void setLoader(Loader loader);

    String getName();

    void setName(String name);

    Container getParent();

    void setParent(Container container);

    void addChild(Container child);

    Container findChild(String name);

    Container[] findChildren();

    void invoke(Request request, Response response) throws IOException, ServletException;

    void removeChild(Container child);

    Logger getLogger();

    void setLogger(Logger logger);
}