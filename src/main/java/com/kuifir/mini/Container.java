package com.kuifir.mini;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface Container {
    static final String ADD_CHILD_EVENT = "addChild";
    static final String REMOVE_CHILD_EVENT = "removeChild";

     String getInfo();

     ClassLoader getLoader();

     void setLoader(ClassLoader loader);

     String getName();

     void setName(String name);

     Container getParent();

     void setParent(Container container);

     void addChild(Container child);

     Container findChild(String name);

     Container[] findChildren();

     void invoke(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;

     void removeChild(Container child);
}