package org.apache.karaf.boot.sample.simple;

import org.apache.karaf.boot.Bean;
import org.apache.karaf.boot.Init;

@Bean(id = "simple-bean")
public class SimpleBean {

    @Init
    public void simple() {
        System.out.println("Hello world");
    }

}
