== karaf-boot-sample-service-provider-ds ==

This sample exposes an OSGi service using the Karaf util classe and annotation.

= Design

The service "contract" is describe by the Hello interface. It's a very simple service that expose one operation (hello).
The service client send a message (String) to the hello service and he gets a response.

The HelloServiceImpl is very simple: it prefixes the message with "Hello".

We use the @Component DS annotation on HelloServiceImpl implementation in order to expose the service.

You don't think anything else: karaf-boot will generate all the plumbing for you, and you will directly have a ready
to use artifact.

= Build

To build, simply do:

  mvn clean install

= Deploy

To deploy in Karaf, you have to enable the DS support first. For that, you have to install the scr feature:

  feature:install scr

Once scr feature installed:

* you can drop the generated jar file (target/karaf-boot-sample-service-provider-ds-1.0.jar) in the
Karaf deploy folder
* in the Karaf shell console, do:

  bundle:install -s mvn:org.apache.karaf.boot/karaf-boot-sample-service-provider-ds/1.0