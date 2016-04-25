== karaf-boot-sample-config ==

This sample shows how to use a configuration provided in the etc folder of Karaf, and directly use the
properties values in your code.

= Design

The ConfigComponent use a SampleConfig configuration. The SampleConfig configuration is "injected" at activation
time of the component.

The component just displays the values of the properties.

= Build

To build, simply do:

  mvn clean install

= Deploy

To deploy in Karaf, you have to enable the DS support first. For that, you have to install the scr feature:

  feature:install scr

Once scr feature installed:

* you can drop the generated jar file (target/karaf-boot-sample-config-1.0.jar) in the
Karaf deploy folder
* in the Karaf shell console, do:

  bundle:install -s mvn:org.apache.karaf.boot/karaf-boot-sample-config/1.0