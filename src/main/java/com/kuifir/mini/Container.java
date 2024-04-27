package com.kuifir.mini;

import com.kuifir.mini.core.WebappClassLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Container {
    static final String ADD_CHILD_EVENT = "addChild";
    static final String REMOVE_CHILD_EVENT = "removeChild";

    String getInfo();

    WebappClassLoader getLoader();

    void setLoader(WebappClassLoader loader);

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