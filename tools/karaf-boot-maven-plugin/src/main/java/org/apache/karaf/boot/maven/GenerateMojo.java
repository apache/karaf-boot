package org.apache.karaf.boot.maven;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.karaf.boot.plugin.api.BootPlugin;
import org.apache.karaf.boot.plugin.api.StreamFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.xbean.finder.ClassFinder;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.plexus.build.incremental.BuildContext;

@Mojo(name = "generate", threadSafe = true, defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, inheritByDefault = false)
public class GenerateMojo extends AbstractMojo {

    private final class BuildStreamFactory implements StreamFactory {
        @Override
        public OutputStream create(File file) {
            try {
                file.getParentFile().mkdirs();
                return buildContext.newFileOutputStream(file);
            } catch (IOException e) {
                throw new RuntimeException("Error creating file " + file, e);
            }
        }
    }

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;
    
    @Parameter(defaultValue="${project}", required=true)
    protected MavenProject project;
    
    @Component
    private BuildContext buildContext;

    public void execute() throws MojoExecutionException {
        try {
            File buildDir = new File(project.getBuild().getDirectory());
            File generatedDir = new File(buildDir, "generated-resources");
            Resource resource = new Resource();
            resource.setDirectory(generatedDir.getPath());
            project.addResource(resource);
            ClassFinder finder = createProjectScopeFinder();
            List<Class<? extends BootPlugin>> plugins = finder.findImplementations(BootPlugin.class);
            Map<String, List<String>> combined = new HashMap<String, List<String>>();
            for (Class<? extends BootPlugin> pluginClass : plugins) {
                BootPlugin plugin = pluginClass.newInstance();
                Class<? extends Annotation> annotation = plugin.getAnnotation();
                List<Class<?>> classes = finder.findAnnotatedClasses(annotation);
                if (!classes.isEmpty()) {
                    Map<String, List<String>> headers = plugin.enhance(classes, generatedDir, new BuildStreamFactory());
                    combine(combined, headers);
                }
            }
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            headers.put("Import-Package", Arrays.asList("*"));
            combine(combined, headers);
            File bndInst = new File(buildDir, "org.apache.karaf.boot.bnd");
            writeBndFile(bndInst, combined);

            InputStream is = this.getClass().getResourceAsStream("/configuration.xml");
            MojoExecution execution = new MojoExecution(getBundleMojo(), Xpp3DomBuilder.build(is, "utf-8"));
            pluginManager.executeMojo(mavenSession, execution);

        } catch (Exception e) {
            throw new MojoExecutionException("karaf-boot-maven-plugin failed", e);
        }
    }

    private MojoDescriptor getBundleMojo() throws PluginNotFoundException, PluginResolutionException,
        PluginDescriptorParsingException, InvalidPluginDescriptorException {
        getLog().info("Invoking maven-bundle-plugin");
        Plugin felixBundlePlugin = new Plugin();
        felixBundlePlugin.setGroupId("org.apache.felix");
        felixBundlePlugin.setArtifactId("maven-bundle-plugin");
        felixBundlePlugin.setVersion("3.0.0");
        felixBundlePlugin.setInherited(true);
        felixBundlePlugin.setExtensions(true);
        PluginDescriptor felixBundlePluginDescriptor = pluginManager.loadPlugin(felixBundlePlugin, mavenProject.getRemotePluginRepositories(), mavenSession.getRepositorySession());
        MojoDescriptor felixBundleMojoDescriptor = felixBundlePluginDescriptor.getMojo("bundle");
        return felixBundleMojoDescriptor;
    }

    private void writeBndFile(File bndInst, Map<String, List<String>> combined) throws IOException {
        try (
            OutputStream os = buildContext.newFileOutputStream(bndInst);
            OutputStreamWriter writer = new OutputStreamWriter(os, "utf-8")
            ) {
            for (String key : combined.keySet()) {
                writer.append(String.format("%s: %s\n", key, String.join(",", combined.get(key))));
            }
        }
    }

    private void combine(Map<String, List<String>> combined, Map<String, List<String>> headers) {
        for (String key : headers.keySet()) {
            List<String> values = headers.get(key);
            if (!combined.containsKey(key)) {
                combined.put(key, new ArrayList<>(values));
            } else {
                List<String> cValues = combined.get(key);
                cValues.addAll(values);
            }
        }
    }

    private ClassFinder createProjectScopeFinder() throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();

        urls.add(new File(project.getBuild().getOutputDirectory()).toURI().toURL());
        for (Object artifactO : project.getArtifacts()) {
            Artifact artifact = (Artifact) artifactO;
            File file = artifact.getFile();
            if (file != null) {
                urls.add(file.toURI().toURL());
            }
        }
        ClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
        return new ClassFinder(loader, urls);
    }

}
