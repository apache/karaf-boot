== karaf-boot-sample-service-blueprint-annotation ==

This sample exposes an OSGi service using blueprint annotations.

= Design

TODO

= Build

To build, simply do:

  mvn clean install

= Deploy

* you can drop the generated jar file (target/karaf-boot-sample-service-blueprint-annotation-1.0.jar) in the
Karaf deploy folder
* in the Karaf shell console, do:

  bundle:install -s mvn:org.apache.karaf.boot/karaf-boot-sample-service-blueprint-annotation/1.0
