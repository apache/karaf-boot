== karaf-boot-sample-service-consumer-osgi ==

This sample uses an OSGi service using the Karaf util classe and annotation.

= Design

The service "contract" is exposed by another artifact (the karaf-boot-sample-service-provider-osgi module).

The hello service is retrieved in the Activator of this artifact, and uses it directly.

This Activator overrides the doStart() method, where we retrieve the HelloService using the getTrackedService() method. Karaf
deals with all service lookup and tracking.

= Build

To build, simply do:

  mvn clean install

= Deploy

You have to install a hello service provider first. Please deploy the karaf-boot-sample-service-provider-osgi first.

To deploy in Karaf:

* you can drop the generated jar file (target/karaf-boot-sample-service-provider-osgi-1.0.jar) in the
Karaf deploy folder
* in the Karaf shell console, do:

  bundle:install -s mvn:karaf-boot-samples/karaf-boot-sample-service-consumer-osgi/1.0