package org.apache.karaf.boot.tools.maven.plugin.model;

public class ProducedBean extends Bean {
    public String factoryMethod;
    public String factoryBeanId;
    
    public ProducedBean(Class<?> clazz, String factoryBeanId, String factoryMethod) {
        super(clazz);
        this.factoryBeanId = factoryBeanId;
        this.factoryMethod = factoryMethod;
    }

}
