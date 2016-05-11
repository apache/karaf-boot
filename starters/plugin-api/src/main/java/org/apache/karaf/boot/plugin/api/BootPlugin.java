package org.apache.karaf.boot.plugin.api;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

public interface BootPlugin {
    Class<? extends Annotation> getAnnotation();
    Map<String, List<String>> enhance(List<Class<?>> annotated, File generatedDir, StreamFactory streamFactory);
}
