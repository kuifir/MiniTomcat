package test;


import com.kuifir.mini.ContainerEvent;
import com.kuifir.mini.ContainerListener;

public class TestListener implements ContainerListener {
    @Override
    public void containerEvent(ContainerEvent event) {
        System.out.println(event);
    }
}
