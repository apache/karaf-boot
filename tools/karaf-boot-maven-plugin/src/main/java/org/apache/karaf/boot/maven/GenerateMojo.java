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
import java.util.Enumeration;
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
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.xbean.finder.ClassFinder;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.plexus.build.incremental.BuildContext;

@Mojo(name = "generate", threadSafe = true, defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, inheritByDefault = false)
public class GenerateMojo extends AbstractMojo {

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
            File generatedDir = createGeneratedDir(buildDir);
            
            URLClassLoader loader = projectClassLoader();
            handleBootPlugins(loader, buildDir, generatedDir);
            handleMavenPlugins(loader);

            runMavenBundlePlugin();

        } catch (Exception e) {
            throw new MojoExecutionException("karaf-boot-maven-plugin failed", e);
        }
    }

    private void handleMavenPlugins(URLClassLoader loader) throws Exception {
        Enumeration<URL> resources = loader.getResources("karaf-boot/plugins.xml");
        while (resources.hasMoreElements()) {
            URL res = resources.nextElement();
            executePluginDef(res.openStream());
        }
    }

    private void executePluginDef(InputStream is) throws Exception {
        Xpp3Dom pluginDef = Xpp3DomBuilder.build(is, "utf-8");
        Plugin plugin = loadPlugin(pluginDef);
        Xpp3Dom config = pluginDef.getChild("configuration");
        PluginDescriptor pluginDesc = pluginManager.loadPlugin(plugin, 
                                                               mavenProject.getRemotePluginRepositories(), 
                                                               mavenSession.getRepositorySession());
        Xpp3Dom executions = pluginDef.getChild("executions");
        
        for ( Xpp3Dom execution : executions.getChildren()) {
            Xpp3Dom goals = execution.getChild("goals");
            for (Xpp3Dom goal : goals.getChildren()) {
                MojoDescriptor desc = pluginDesc.getMojo(goal.getValue());
                pluginManager.executeMojo(mavenSession, new MojoExecution(desc, config));
            }
        }
    }

    Plugin loadPlugin(Xpp3Dom pluginDef) {
        String groupId = pluginDef.getChild("groupId").getValue();
        String artifactId = pluginDef.getChild("artifactId").getValue();
        String version = pluginDef.getChild("version").getValue();
        Plugin plugin = new Plugin();
        plugin.setGroupId(groupId);
        plugin.setArtifactId(artifactId);
        plugin.setVersion(version);
        return plugin;
    }

    private File createGeneratedDir(File buildDir) {
        File generatedDir = new File(buildDir, "generated-resources");
        Resource resource = new Resource();
        resource.setDirectory(generatedDir.getPath());
        project.addResource(resource);
        return generatedDir;
    }

    private void handleBootPlugins(URLClassLoader loader, File buildDir, File generatedDir)
        throws MalformedURLException, InstantiationException, IllegalAccessException, IOException {
        ClassFinder finder = new ClassFinder(loader, Arrays.asList(loader.getURLs()));
        List<Class<? extends BootPlugin>> plugins = finder.findImplementations(BootPlugin.class);
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        for (Class<? extends BootPlugin> pluginClass : plugins) {
            applyBootPlugin(generatedDir, finder, headers, pluginClass);
        }
        addImportDefault(headers);
        File bndInst = new File(buildDir, "org.apache.karaf.boot.bnd");
        writeBndFile(bndInst, headers);
    }

    private void applyBootPlugin(File generatedDir, ClassFinder finder, Map<String, List<String>> combined,
                                 Class<? extends BootPlugin> pluginClass)
        throws InstantiationException, IllegalAccessException {
        BootPlugin plugin = pluginClass.newInstance();
        Class<? extends Annotation> annotation = plugin.getAnnotation();
        List<Class<?>> classes = finder.findAnnotatedClasses(annotation);
        if (!classes.isEmpty()) {
            Map<String, List<String>> headers = plugin.enhance(classes, generatedDir, new BuildStreamFactory());
            combine(combined, headers);
        }
    }

    private void addImportDefault(Map<String, List<String>> combined) {
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Import-Package", Arrays.asList("*"));
        combine(combined, headers);
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

    private URLClassLoader projectClassLoader() throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        urls.add(new File(project.getBuild().getOutputDirectory()).toURI().toURL());
        for (Artifact artifact : project.getArtifacts()) {
            File file = artifact.getFile();
            if (file != null) {
                urls.add(file.toURI().toURL());
            }
        }
        URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
        return loader;
    }

    private void runMavenBundlePlugin() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/configuration.xml");
        MojoExecution execution = new MojoExecution(getMavenBundleMojo(), Xpp3DomBuilder.build(is, "utf-8"));
        getLog().info("Invoking maven-bundle-plugin");
        pluginManager.executeMojo(mavenSession, execution);
    }

    private MojoDescriptor getMavenBundleMojo() throws Exception {
        Plugin plugin = new Plugin();
        plugin.setGroupId("org.apache.felix");
        plugin.setArtifactId("maven-bundle-plugin");
        plugin.setVersion("3.0.0");
        plugin.setInherited(true);
        plugin.setExtensions(true);
        PluginDescriptor desc = pluginManager.loadPlugin(plugin, mavenProject.getRemotePluginRepositories(), mavenSession.getRepositorySession());
        return desc.getMojo("bundle");
    }
    

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
}
