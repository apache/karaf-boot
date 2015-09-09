package org.apache.karaf.boot.tools.maven.plugin.test;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.osgi.framework.BundleContext;

@Singleton
public class MyBean4 {

    @Inject
    BundleContext bundleContext;
}
