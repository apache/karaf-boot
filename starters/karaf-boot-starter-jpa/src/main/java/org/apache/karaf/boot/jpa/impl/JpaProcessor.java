package org.apache.karaf.boot.jpa.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.karaf.boot.jpa.PersistentUnit;
import org.apache.karaf.boot.jpa.Property;
import org.apache.karaf.boot.jpa.Provider;
import org.apache.karaf.boot.plugin.api.BootPlugin;
import org.apache.karaf.boot.plugin.api.StreamFactory;

import javanet.staxutils.IndentingXMLStreamWriter;

public class JpaProcessor implements BootPlugin {

    private boolean useHibernate;

    public JpaProcessor() {
    }

    @Override
    public Map<String, List<String>> enhance(List<Class<?>> annotatedList, File generatedDir,
                                StreamFactory streamFactory) {
        try {
            File persistenceFile = new File(generatedDir, "META-INF/persistence.xml");
            OutputStream os = streamFactory.create(persistenceFile);
            process(new OutputStreamWriter(os), annotatedList);
            // System.out.println(Kind.NOTE, "Generated META-INF/persistence.xml");
        } catch (Exception e) {
            throw new RuntimeException(e);
            // processingEnv.getMessager().printMessage(Kind.ERROR, "Error: " + e.getMessage());
        }
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("Meta-Persistence", Arrays.asList("META-INF/persistence.xml"));
        if (useHibernate) {
            headers.put("Import-Package", Arrays.asList("org.hibernate.proxy", "javassist.util.proxy"));
        }
        return headers;
    }

    public void process(Writer writer, List<Class<?>> annotatedList) throws Exception {
        Set<String> puNames = new HashSet<String>();
        XMLOutputFactory xof = XMLOutputFactory.newInstance();
        XMLStreamWriter w = new IndentingXMLStreamWriter(xof.createXMLStreamWriter(writer));
        w.setDefaultNamespace("http://java.sun.com/xml/ns/persistence");
        w.writeStartDocument();
        w.writeStartElement("persistence");
        w.writeAttribute("verson", "2.0");

        // w.println("<persistence version=\"2.0\" xmlns=\"http://java.sun.com/xml/ns/persistence\"
        // xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
        // xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence
        // http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\">");
        for (Class<?> annotated : annotatedList) {
            PersistentUnit pu = annotated.getAnnotation(PersistentUnit.class);
            if (pu.name() == null || pu.name().isEmpty()) {
                throw new IOException("Missing persistent unit name");
            }
            if (!puNames.add(pu.name())) {
                throw new IOException("Duplicate persistent unit name: " + pu.name());
            }
            w.writeStartElement("persistence-unit");
            w.writeAttribute("name", pu.name());
            w.writeAttribute("transaction-type", pu.transactionType().toString());
            writeElement(w, "description", pu.description());
            String providerName = getProvider(pu);
            writeElement(w, "provider", providerName);
            writeElement(w, "jta-data-source", pu.jtaDataSource());
            writeElement(w, "non-jta-data-source", pu.nonJtaDataSource());
            Map<String, String> props = new HashMap<>();
            addProperties(pu, props);
            addAnnProperties(annotated, props);
            if (props.size() > 0) {
                w.writeStartElement("properties");
                for (String key : props.keySet()) {
                    w.writeEmptyElement("property");
                    w.writeAttribute("name", key);
                    w.writeAttribute("value", props.get(key));
                }
                w.writeEndElement();
            }
            w.writeEndElement();
        }
        w.writeEndElement();
        w.writeEndDocument();
        w.flush();
        w.close();
    }

    private void addProperties(PersistentUnit pu, Map<String, String> props) {
        if (pu.properties() == null) {
            return;
        }
        for (Property property : pu.properties()) {
            props.put(property.name(), property.value());
        }
    }

    private void addAnnProperties(Class<?> annotated, Map<String, String> props) throws XMLStreamException {
        for (Annotation annotation : annotated.getAnnotations()) {

            String name = null;
            /*
             * for (Annotation a : annotated.getAnnotations()) { if (a.
             * toString().startsWith("@org.apache.karaf.boot.jpa.PersistentUnit.ProviderProperty")) { name =
             * a.getElementValues().values().iterator().next().getValue().toString(); break; } } if (name !=
             * null) { String value =
             * annMirror.getElementValues().values().iterator().next().getValue().toString(); props.put(name,
             * value); }
             */
            // processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation: " + annMirror);
            // processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation type: " +
            // annMirror.getAnnotationType());
            // processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation annot: " +
            // annMirror.getAnnotationType().getAnnotationMirrors());
            // processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation annot: " +
            // processingEnv.getElementUtils().getAllAnnotationMirrors(annMirror.getAnnotationType().asElement()));
            // processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation values: " +
            // annMirror.getElementValues());
            // if (annMirror.getAnnotationType().getAnnotation(PersistentUnit.ProviderProperty.class) != null)
            // {
            // processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation ok");
            // } else {
            // processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation nok");
            // }
        }
    }

    private void writeElement(XMLStreamWriter w, String localName, String content) throws XMLStreamException {
        if (content != null && !content.isEmpty()) {
            w.writeStartElement(localName);
            w.writeCharacters(content);
            w.writeEndElement();
        }
    }

    private String getProvider(PersistentUnit pu) throws IOException {
        if (pu.provider() != Provider.Default && pu.providerName() != null && !pu.providerName().isEmpty()) {
            throw new IOException("At most one of provider and providerName can be used");
        }
        if (pu.provider() != null) {
            switch (pu.provider()) {
            case Hibernate:
                useHibernate = true;
                return "org.hibernate.jpa.HibernatePersistenceProvider";
            default:
                // TODO
                throw new IOException("Unsupported provider: " + pu.provider());
            }
        } else if (pu.providerName() != null) {
            return pu.providerName();
        } else {
            return null;
        }
    }

    @Override
    public Class<? extends Annotation> getAnnotation() {
        return PersistentUnit.class;
    }

}
