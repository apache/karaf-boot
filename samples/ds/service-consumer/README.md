== karaf-boot-sample-service-provider-osgi ==

This sample exposes an OSGi service using the Karaf util classe and annotation.

= Design

This artifact uses the hello service provided by another artifact (karaf-boot-sample-service-provider-ds for instance).

It uses the DS annotations to create a bean with a reference (@Reference) to the hello service.
In the HelloServiceClient bean, we use the @Activate annotation to execute a specific method.

You don't think anything else: karaf-boot will generate all the plumbing for you, and you will directly have a ready
to use artifact.

= Build

To build, simply do:

  mvn clean install

= Deploy

To deploy in Karaf, you have to enable the DS support first. For that, you have to install the scr feature:

  feature:install scr

Once scr feature installed, you have to install a hello service provider. Please use any of karaf-boot-sample-service-provider-*
deployment first.

Once the service provider is installed:

* you can drop the generated jar file (target/karaf-boot-sample-service-consumer-ds-1.0.jar) in the
Karaf deploy folder
* in the Karaf shell console, do:

  bundle:install -s mvn:org.apache.karaf.boot/karaf-boot-sample-service-consumer-ds/1.0