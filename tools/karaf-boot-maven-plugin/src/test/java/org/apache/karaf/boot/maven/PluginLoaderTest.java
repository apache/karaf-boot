package org.apache.karaf.boot.maven;

import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Test;

public class PluginLoaderTest {

    @Test
    public void testLoadXml() throws XmlPullParserException, IOException {
        InputStream is = this.getClass().getResourceAsStream("/plugins.xml");
        Xpp3Dom pluginDef = Xpp3DomBuilder.build(is, "utf-8");
        Plugin plugin = new GenerateMojo().loadPlugin(pluginDef);
        Assert.assertEquals("org.apache.aries.blueprint", plugin.getGroupId());
        Assert.assertEquals("blueprint-maven-plugin", plugin.getArtifactId());
        Assert.assertEquals("1.4.0", plugin.getVersion());
    }

}
