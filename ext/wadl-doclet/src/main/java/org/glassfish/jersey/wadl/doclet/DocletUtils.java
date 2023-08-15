/*
 * Copyright (c) 2019, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.wadl.doclet;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ResourceDocType;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class DocletUtils {

    private static final Logger LOG = Logger.getLogger(DocletUtils.class.getName());

    private static String[] getCDataElements(DocProcessor docProcessor) {
        String[] original = new String[]{"commentText"};
        if (docProcessor == null) {
            return original;
        } else {
            String[] cdataElements = docProcessor.getCDataElements();
            if (cdataElements == null || cdataElements.length == 0) {
                return original;
            } else {

                String[] result = copyOf(original, original.length + cdataElements.length);
                for (int i = 0; i < cdataElements.length; i++) {
                    result[original.length + i] = cdataElements[i];
                }
                return result;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, U> T[] copyOf(U[] original, int newLength) {
        T[] copy = (original.getClass() == Object[].class) ? (T[]) new Object[newLength]
                : (T[]) Array.newInstance(original.getClass().getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
        return copy;
    }

    private static Class<?>[] getJAXBContextClasses(ResourceDocType result, DocProcessor docProcessor) {
        Class<?>[] clazzes;
        if (docProcessor == null) {
            clazzes = new Class<?>[1];
        } else {
            Class<?>[] requiredJaxbContextClasses = docProcessor.getRequiredJaxbContextClasses();
            if (requiredJaxbContextClasses != null) {
                clazzes = new Class<?>[1 + requiredJaxbContextClasses.length];
                for (int i = 0; i < requiredJaxbContextClasses.length; i++) {
                    clazzes[i + 1] = requiredJaxbContextClasses[i];
                }
            } else {
                clazzes = new Class<?>[1];
            }
        }
        clazzes[0] = result.getClass();
        return clazzes;
    }

    static boolean createOutputFile(String filePath, DocProcessor docProcessor, ResourceDocType result) {
        String[] cdataElements = getCDataElements(docProcessor);
        Class<?>[] classes = getJAXBContextClasses(result, docProcessor);
        LOG.info("cdataElements " + Arrays.asList(cdataElements));
        LOG.info("classes " + Arrays.asList(classes));
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)))) {
            JAXBContext c = JAXBContext.newInstance(classes);
            Marshaller m = c.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter sw = new StringWriter();
            // Produces XML in memory
            m.marshal(result, sw);
            // Loads the XML from memory for processing
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(sw.toString().getBytes()));
            for (String cdata : cdataElements) {
                NodeList nodes = document.getElementsByTagName(cdata);
                LOG.info(nodes.getLength() + " nodes found by " + cdata);
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    CDATASection cdataSection = document.createCDATASection(node.getTextContent());
                    // Remove current content
                    node.setTextContent(null);
                    // Add it again, but wrapped with CDATA
                    node.appendChild(cdataSection);
                }
                document.createCDATASection(cdata);
            }
            DOMSource source = new DOMSource(document);
            StreamResult streamResult = new StreamResult(out);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, streamResult);
            LOG.info("Wrote " + result + " in " + filePath);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not serialize ResourceDoc.", e);
            return false;
        }
    }

    static String getLinkClass(String className, String field) {
        Object object;
        try {
            Field declaredField = Class.forName(className, false, Thread.currentThread()
                    .getContextClassLoader()).getDeclaredField(field);
            declaredField.setAccessible(true);
            object = declaredField.get(null);
            LOG.log(Level.FINE, "Got object " + object);
        } catch (final Exception e) {
            LOG.info("Have classloader: " + ResourceDoclet.class.getClassLoader().getClass());
            LOG.info("Have thread classloader " + Thread.currentThread().getContextClassLoader().getClass());
            LOG.info("Have system classloader " + ClassLoader.getSystemClassLoader().getClass());
            LOG.log(Level.SEVERE, "Could not get field " + className, e);
            return null;
        }

        /* marshal the bean to xml
         */
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            StringWriter stringWriter = new StringWriter();
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(object, stringWriter);
            String result = stringWriter.getBuffer().toString();
            LOG.log(Level.FINE, "Got marshalled output:\n" + result);
            return result;
        } catch (final Exception e) {
            LOG.log(Level.SEVERE, "Could serialize bean to xml: " + object, e);
            return null;
        }
    }

}
