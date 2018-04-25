/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jaxb.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.WeakHashMap;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Providers;

import javax.inject.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import org.xml.sax.InputSource;

/**
 * String reader provider producing {@link ParamConverterProvider param converter provider} that
 * support conversion of a string value into a JAXB instance.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class JaxbStringReaderProvider {

    private static final Map<Class, JAXBContext> jaxbContexts = new WeakHashMap<Class, JAXBContext>();
    private final Value<ContextResolver<JAXBContext>> mtContext;
    private final Value<ContextResolver<Unmarshaller>> mtUnmarshaller;

    /**
     * Create JAXB string reader provider.
     *
     * @param ps used to obtain {@link JAXBContext} and {@link Unmarshaller} {@link ContextResolver ContextResolvers}
     */
    public JaxbStringReaderProvider(final Providers ps) {
        this.mtContext = Values.lazy(new Value<ContextResolver<JAXBContext>>() {

            @Override
            public ContextResolver<JAXBContext> get() {
                return ps.getContextResolver(JAXBContext.class, null);
            }
        });

        this.mtUnmarshaller = Values.lazy(new Value<ContextResolver<Unmarshaller>>() {
            @Override
            public ContextResolver<Unmarshaller> get() {
                return ps.getContextResolver(Unmarshaller.class, null);
            }
        });
    }

    /**
     * Get JAXB unmarshaller for the type.
     *
     * @param type Java type to be unmarshalled.
     * @return JAXB unmarshaller for the given type.
     * @throws JAXBException in case there's an error retrieving the unmarshaller.
     */
    protected final Unmarshaller getUnmarshaller(Class type) throws JAXBException {
        final ContextResolver<Unmarshaller> unmarshallerContextResolver = mtUnmarshaller.get();
        if (unmarshallerContextResolver != null) {
            Unmarshaller u = unmarshallerContextResolver.getContext(type);
            if (u != null) {
                return u;
            }
        }
        return getJAXBContext(type).createUnmarshaller();
    }

    private JAXBContext getJAXBContext(Class type) throws JAXBException {
        final ContextResolver<JAXBContext> jaxbContextContextResolver = mtContext.get();
        if (jaxbContextContextResolver != null) {
            JAXBContext c = jaxbContextContextResolver.getContext(type);
            if (c != null) {
                return c;
            }
        }
        return getStoredJAXBContext(type);
    }

    /**
     * Get the stored JAXB context supporting the Java type.
     *
     * @param type Java type supported by the stored JAXB context.
     * @return stored JAXB context supporting the Java type.
     * @throws JAXBException in case JAXB context retrieval fails.
     */
    protected JAXBContext getStoredJAXBContext(Class type) throws JAXBException {
        synchronized (jaxbContexts) {
            JAXBContext c = jaxbContexts.get(type);
            if (c == null) {
                c = JAXBContext.newInstance(type);
                jaxbContexts.put(type, c);
            }
            return c;
        }
    }

    /**
     * Root element JAXB {@link ParamConverter param converter}.
     */
    public static class RootElementProvider extends JaxbStringReaderProvider implements ParamConverterProvider {

        private final Provider<SAXParserFactory> spfProvider;

        /**
         * Creates new instance.
         *
         * @param spfProvider {@link SAXParserFactory SAX parser factory} injection provider.
         * @param ps used to obtain {@link JAXBContext} and {@link Unmarshaller} {@link ContextResolver ContextResolvers}
         */
        public RootElementProvider(@Context Provider<SAXParserFactory> spfProvider, @Context Providers ps) {
            super(ps);
            this.spfProvider = spfProvider;
        }


        @Override
        public <T> ParamConverter<T> getConverter(final Class<T> rawType, Type genericType, Annotation[] annotations) {
            final boolean supported = (rawType.getAnnotation(XmlRootElement.class) != null
                    || rawType.getAnnotation(XmlType.class) != null);
            if (!supported) {
                return null;
            }

            return new ParamConverter<T>() {

                @Override
                public T fromString(String value) {
                    try {
                        final SAXSource source = new SAXSource(
                                spfProvider.get().newSAXParser().getXMLReader(),
                                new InputSource(new java.io.StringReader(value)));

                        final Unmarshaller u = getUnmarshaller(rawType);
                        if (rawType.isAnnotationPresent(XmlRootElement.class)) {
                            return rawType.cast(u.unmarshal(source));
                        } else {
                            return u.unmarshal(source, rawType).getValue();
                        }
                    } catch (UnmarshalException ex) {
                        throw new ExtractorException(LocalizationMessages.ERROR_UNMARSHALLING_JAXB(rawType), ex);
                    } catch (JAXBException ex) {
                        throw new ProcessingException(LocalizationMessages.ERROR_UNMARSHALLING_JAXB(rawType), ex);
                    } catch (Exception ex) {
                        throw new ProcessingException(LocalizationMessages.ERROR_UNMARSHALLING_JAXB(rawType), ex);
                    }
                }

                @Override
                public String toString(T value) throws IllegalArgumentException {
                    // TODO: JERSEY-1385
                    return "test";
                }
            };
        }
    }
}
