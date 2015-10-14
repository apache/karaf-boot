== karaf-boot-sample-service-provider-osgi ==

This sample exposes an OSGi service using the Karaf util classe and annotation.

= Design

The service "contract" is describe by the Hello interface. It's a very simple service that expose one operation (hello).
The service client send a message (String) to the hello service and he gets a response.

The HelloServiceImpl is very simple: it prefixes the message with "Hello".

In order to expose this service, we create an Activator, extending Karaf util BaseActivator.
Our activator contains the @Services annotation describing the provided services (@ProvideService annotation).

In this activator, we override the doStart() method, where we instantiate the HelloServiceImpl bean and register the
HelloService service using the register() method (provided by Karaf). The Karaf BaseActivator manages the service
registration, so you don't have to take care about the unregistration of the service, etc.

= Build

To build, simply do:

  mvn clean install

= Deploy

To deploy in Karaf:

* you can drop the generated jar file (target/karaf-boot-sample-service-provider-osgi-1.0.jar) in the
Karaf deploy folder
* in the Karaf shell console, do:

  bundle:install -s mvn:org.apache.karaf.boot/karaf-boot-sample-service-provider-osgi/1.0