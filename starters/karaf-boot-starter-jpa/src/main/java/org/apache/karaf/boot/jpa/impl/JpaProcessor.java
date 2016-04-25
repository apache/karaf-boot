package org.apache.karaf.boot.jpa.impl;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.karaf.boot.jpa.PersistentUnit;
import org.apache.karaf.boot.jpa.Property;
import org.apache.karaf.boot.jpa.Provider;

public class JpaProcessor extends AbstractProcessor {

    public JpaProcessor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>(Arrays.asList(
                PersistentUnit.class.getName()
        ));
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<PersistentUnit, List<? extends AnnotationMirror>> units = new HashMap<PersistentUnit, List<? extends AnnotationMirror>>();


        for (Element elem : roundEnv.getElementsAnnotatedWith(PersistentUnit.class)) {
            PersistentUnit pu = elem.getAnnotation(PersistentUnit.class);
            units.put(pu, elem.getAnnotationMirrors());
        }
        if (!units.isEmpty()) {
            try {
                Set<String> puNames = new HashSet<String>();
                FileObject o = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                        "", "META-INF/persistence.xml");
                PrintWriter w = new PrintWriter(o.openWriter());
                w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                w.println("<persistence version=\"2.0\" xmlns=\"http://java.sun.com/xml/ns/persistence\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd\">");
                for (PersistentUnit pu : units.keySet()) {
                    if (pu.name() == null || pu.name().isEmpty()) {
                        throw new IOException("Missing persistent unit name");
                    }
                    if (!puNames.add(pu.name())) {
                        throw new IOException("Duplicate persistent unit name: " + pu.name());
                    }
                    w.println("    <persistence-unit name=\"" + pu.name() + "\" transaction-type=\"" + pu.transactionType().toString() + "\">");
                    if (!pu.description().isEmpty()) {
                        w.println("        <description>" + pu.description() + "</description>");
                    }
                    if (pu.provider() != Provider.Default || !pu.providerName().isEmpty()) {
                        if (pu.provider() != Provider.Default && !pu.providerName().isEmpty()) {
                            throw new IOException("At most one of provider and providerName can be used");
                        }
                        String name;
                        if (!pu.providerName().isEmpty()) {
                            name = pu.providerName();
                        } else {
                            switch (pu.provider()) {
                                case Hibernate:
                                    name = "org.hibernate.jpa.HibernatePersistenceProvider";
                                    break;
                                default:
                                    // TODO
                                    throw new IOException("Unsupported provider: " + pu.provider());
                            }
                        }
                        w.println("        <provider>" + name + "</provider>");
                    }
                    if (!pu.jtaDataSource().isEmpty()) {
                        w.println("        <jta-data-source>" + pu.jtaDataSource() + "</jta-data-source>");
                    }
                    if (!pu.nonJtaDataSource().isEmpty()) {
                        w.println("        <non-jta-data-source>" + pu.nonJtaDataSource() + "</non-jta-data-source>");
                    }
                    if (pu.properties().length > 0) {
                        w.println("        <properties>");
                        for (Property property : pu.properties()) {
                            w.println("            <property name=\"" + property.name() + "\" value=\"" + property.value() + "\"/>");
                        }


                        for (AnnotationMirror annMirror : units.get(pu)) {

                            String name = null;
                            for (AnnotationMirror a : processingEnv.getElementUtils().getAllAnnotationMirrors(annMirror.getAnnotationType().asElement())) {
                                if (a.toString().startsWith("@org.apache.karaf.boot.jpa.PersistentUnit.ProviderProperty")) {
                                    name = a.getElementValues().values().iterator().next().getValue().toString();
                                    break;
                                }
                            }
                            if (name != null) {
                                String value = annMirror.getElementValues().values().iterator().next().getValue().toString();
                                w.println("            <property name=\"" + name + "\" value=\"" + value + "\"/>");
                            }
//                            processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation: " + annMirror);
//                            processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation type: " + annMirror.getAnnotationType());
//                            processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation annot: " + annMirror.getAnnotationType().getAnnotationMirrors());
//                            processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation annot: " + processingEnv.getElementUtils().getAllAnnotationMirrors(annMirror.getAnnotationType().asElement()));
//                            processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation values: " + annMirror.getElementValues());
//                            if (annMirror.getAnnotationType().getAnnotation(PersistentUnit.ProviderProperty.class) != null) {
//                                processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation ok");
//                            } else {
//                                processingEnv.getMessager().printMessage(Kind.MANDATORY_WARNING, "Annotation nok");
//                            }
                        }

                        w.println("        </properties>");
                    }
                    w.println("    </persistence-unit>");
                }
                w.println("</persistence>");
                w.close();
                processingEnv.getMessager().printMessage(Kind.NOTE, "Generated META-INF/persistence.xml");
            } catch (IOException e) {
                processingEnv.getMessager().printMessage(Kind.ERROR, "Error: " + e.getMessage());
            }
        }
        return true;
    }

}
