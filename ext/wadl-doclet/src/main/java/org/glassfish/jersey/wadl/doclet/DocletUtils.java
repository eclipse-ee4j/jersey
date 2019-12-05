/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.glassfish.jersey.server.wadl.internal.generators.resourcedoc.model.ResourceDocType;

class DocletUtils {

    private static final Logger LOG = Logger.getLogger(DocletUtils.class.getName());

    private static String[] getCDataElements(DocProcessor docProcessor) {
        String[] original = new String[]{"ns1^commentText", "ns2^commentText", "^commentText" };
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

    private static XMLSerializer getXMLSerializer(OutputStream os, String[] cdataElements)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        // configure an OutputFormat to handle CDATA
        OutputFormat of = new OutputFormat();

        // specify which of your elements you want to be handled as CDATA.
        // The use of the '^' between the namespaceURI and the localname
        // seems to be an implementation detail of the xerces code.
        // When processing xml that doesn't use namespaces, simply omit the
        // namespace prefix as shown in the third CDataElement below.
        of.setCDataElements(cdataElements);

        // set any other options you'd like
        of.setPreserveSpace(true);
        of.setIndenting(true);

        // create the serializer
        XMLSerializer serializer = new XMLSerializer(of);

        serializer.setOutputByteStream(os);

        return serializer;
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
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(filePath))) {
            Class<?>[] clazzes = getJAXBContextClasses(result, docProcessor);
            JAXBContext c = JAXBContext.newInstance(clazzes);
            Marshaller m = c.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            String[] cdataElements = getCDataElements(docProcessor);
            XMLSerializer serializer = getXMLSerializer(out, cdataElements);
            m.marshal(result, serializer);
            LOG.info("Wrote " + result);
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
