package org.apache.karaf.boot.tools.maven.plugin.test;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MyFactoryBean {
    
    @Inject
    ServiceB serviceB;

    @Produces
    public MyProduced create() {
        return new MyProduced("My message");
    }
}
