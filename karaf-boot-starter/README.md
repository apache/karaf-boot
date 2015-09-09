Karaf Boot Starter
------------------
Karaf Boot Starter provides a convenient way to create a ready to execute artifact, embedding Karaf.

You just have to extend the KarafApplication class. In the config() method, you can define:
- the configuration
- the bundles
- the features
of your embedded Karaf instance.

The Karaf Boot Starter Maven plugin will create the "key turn" jar that you can execute directly, on any machine.