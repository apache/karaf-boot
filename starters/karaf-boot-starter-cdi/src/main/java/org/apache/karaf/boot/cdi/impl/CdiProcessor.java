package org.apache.karaf.boot.cdi.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.karaf.boot.plugin.api.BootPlugin;
import org.apache.karaf.boot.plugin.api.StreamFactory;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;

public class CdiProcessor implements BootPlugin {
    
    @Override
    public Class<? extends Annotation> getAnnotation() {
        return OsgiServiceProvider.class;
    }

    @Override
    public Map<String, List<String>> enhance(List<Class<?>> annotated, File generatedDir,
                                             StreamFactory streamFactory) {
        OutputStream beansOS = streamFactory.create(new File (generatedDir, "META-INF/beans.xml"));
        try {
            beansOS.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing stream", e);
        }
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Require-Capability", Arrays.asList("osgi.extender; filter:=\"(osgi.extender=pax.cdi)\""));
        return headers;
    }


}
