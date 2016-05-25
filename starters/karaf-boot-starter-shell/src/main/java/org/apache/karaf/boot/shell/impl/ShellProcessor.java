package org.apache.karaf.boot.shell.impl;

import static java.util.stream.Collectors.toCollection;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.karaf.boot.plugin.api.BootPlugin;
import org.apache.karaf.boot.plugin.api.StreamFactory;
import org.apache.karaf.shell.api.action.lifecycle.Service;

public class ShellProcessor implements BootPlugin {
    
    @Override
    public Class<? extends Annotation> getAnnotation() {
        return Service.class;
    }

    @Override
    public Map<String, List<String>> enhance(List<Class<?>> annotated, File generatedDir,
                                             StreamFactory streamFactory) {
        Set<String> packages = annotated.stream() //
            .map(clazz -> clazz.getPackage().getName()) //
            .collect(toCollection(TreeSet::new)); 
            new TreeSet<>();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Require-Capability", new ArrayList<>(packages));
        return headers;
    }

}
