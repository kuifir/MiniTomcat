package com.kuifir.mini;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Pipeline 表示的是 Container 中的 Valve 链条，其中有特殊的 basic。Pipeline 启动 Valve 链条的调用。
 */
public interface Pipeline {
    Valve getBasic();

    void setBasic(Valve valve);

    void addValve(Valve valve);

    Valve[] getValves();

    void invoke(Request request, Response response) throws IOException, ServletException;

    void removeValve(Valve valve);
}