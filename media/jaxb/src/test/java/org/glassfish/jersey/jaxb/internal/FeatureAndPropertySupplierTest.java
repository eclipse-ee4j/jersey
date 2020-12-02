/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.PerThread;
import org.glassfish.jersey.jaxb.FeatureSupplier;
import org.glassfish.jersey.jaxb.PropertySupplier;
import org.glassfish.jersey.message.MessageProperties;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.inject.Singleton;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.Configuration;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.LogManager;

public class FeatureAndPropertySupplierTest {
    private InjectionManager injectionManager;
    private static PrintStream systemErrorStream;
    private static ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

    @BeforeClass
    public static void setup() {
        systemErrorStream = System.err;
        System.setErr(new PrintStream(errorStream));
    }

    @AfterClass
    public static void tearDown() {
        System.setErr(systemErrorStream);
    }

    @Test
    public void xmlReaderDoesNotAllowDocTypeDecl() throws Exception {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(createFeatureSupplier(SAXParserFactory.class,
                        "http://apache.org/xml/features/disallow-doctype-decl", true))
                        .to(FeatureSupplier.class)
                        .ranked(Priorities.USER - 1); //override the SaxParserFactoryInjectionProviderTest.AllowDoctypeDeclFeature
            }
        });

        String url = "file:///no-such-file";
        String content = "<!DOCTYPE x [<!ENTITY % pe SYSTEM '" + url + "'> %pe;]><x/>";

        try {
            injectionManager.getInstance(SAXParserFactory.class).newSAXParser().getXMLReader()
                    .parse(new InputSource(new ByteArrayInputStream(content.getBytes("us-ascii"))));
            Assert.fail("DOCTYPE is NOT disallowed when the feature \"disallow-doctype-decl\" is true");
        } catch (SAXParseException saxe) {
            //expected
        }

    }

    @Test
    public void setPropertyOnSaxParserIsNotRecognised() throws ParserConfigurationException, SAXException {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(createPropertySupplier(SAXParser.class, "Unknown-Property", "Unknown Value")).to(PropertySupplier.class);
            }
        });


        injectionManager.getInstance(SAXParserFactory.class).newSAXParser();
        String warning = new String(errorStream.toByteArray());
        errorStream.reset();
        Assert.assertThat(warning, CoreMatchers.containsString("Cannot set property \"Unknown-Property\""));
    }

    @Test
    public void setPropertyOnInsecureSaxParserIsNotRecognised() throws ParserConfigurationException, SAXException {
        final AtomicReference<Configuration> defaultConfig = new AtomicReference<>(null);
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getName().equals("getProperty") && args[0].equals(MessageProperties.XML_SECURITY_DISABLE)) {
                return true;
            }
            return method.invoke(defaultConfig.get(), args);
        };
        Configuration insecureConfig =
                (Configuration) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{Configuration.class}, handler);
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(createPropertySupplier(SAXParser.class, "Unknown-Property", "Unknown Value")).to(PropertySupplier.class);
                bindFactory(() -> insecureConfig).to(Configuration.class).ranked(Priorities.USER - 1);
            }
        });

        defaultConfig.set(injectionManager.getInstance(Configuration.class));

        injectionManager.getInstance(SAXParserFactory.class).newSAXParser();
        String warning = new String(errorStream.toByteArray());
        errorStream.reset();
        Assert.assertThat(warning, CoreMatchers.containsString("Cannot set property \"Unknown-Property\""));
    }

    @Test
    public void setPropertyOnTransformerFactoryIsNotRecognised() {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(createPropertySupplier(TransformerFactory.class, "Unknown-Property", "Unknown Value"))
                        .to(PropertySupplier.class);
                bindFactory(TransformerFactoryInjectionProvider.class, Singleton.class)
                        .to(TransformerFactory.class).in(PerThread.class);
            }
        });

        injectionManager.getInstance(TransformerFactory.class);
        LogManager.getLogManager().reset();
        String warning = new String(errorStream.toByteArray());
        errorStream.reset();
        Assert.assertThat(warning, CoreMatchers.containsString("Cannot set property \"Unknown-Property\""));
    }

    @Test
    public void setFeatureOnTransformerFactoryIsNotRecognised() {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(createFeatureSupplier(TransformerFactory.class, "Unknown-Feature", true))
                        .to(FeatureSupplier.class);
                bindFactory(TransformerFactoryInjectionProvider.class, Singleton.class)
                        .to(TransformerFactory.class).in(PerThread.class);
            }
        });

        injectionManager.getInstance(TransformerFactory.class);
        String warning = new String(errorStream.toByteArray());
        errorStream.reset();
        Assert.assertThat(warning, CoreMatchers.containsString("Cannot set feature \"Unknown-Feature\""));
    }

    @Test
    public void setPropertyOnXmlInputStreamIsNotRecognised() {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(createPropertySupplier(XMLInputFactory.class, "Unknown-Property", "Unknown Value"))
                        .to(PropertySupplier.class);
                bindFactory(XmlInputFactoryInjectionProvider.class, Singleton.class)
                        .to(XMLInputFactory.class).in(PerThread.class);
            }
        });

        injectionManager.getInstance(XMLInputFactory.class);
        String warning = new String(errorStream.toByteArray());
        errorStream.reset();
        Assert.assertThat(warning, CoreMatchers.containsString("Cannot set property \"Unknown-Property\""));
    }

    @Test
    public void setPropertyOnDocumentBuilderFactoryIsNotRecognised() {
        injectionManager = SaxParserFactoryInjectionProviderTest.createInjectionManager(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(createPropertySupplier(DocumentBuilderFactory.class, "Unknown-Property", "Unknown Value"))
                        .to(PropertySupplier.class);
                bindFactory(DocumentBuilderFactoryInjectionProvider.class, Singleton.class)
                        .to(DocumentBuilderFactory.class).in(PerThread.class);
            }
        });

        injectionManager.getInstance(DocumentBuilderFactory.class);
        String warning = new String(errorStream.toByteArray());
        errorStream.reset();
        Assert.assertThat(warning, CoreMatchers.containsString("Cannot set property \"Unknown-Property\""));

    }

    private static PropertySupplier createPropertySupplier(Class<?> clazz, String key, Object value) {
        return new PropertySupplier() {
            @Override
            public boolean isFor(Class<?> factoryOrParserClass) {
                return clazz == factoryOrParserClass;
            }

            @Override
            public Map<String, Object> getProperties() {
                return Collections.singletonMap(key, value);
            }
        };
    }

    private static FeatureSupplier createFeatureSupplier(Class<?> clazz, String key, Boolean value) {
        return new FeatureSupplier() {
            @Override
            public boolean isFor(Class<?> factoryOrParserClass) {
                return clazz == factoryOrParserClass;
            }

            @Override
            public Map<String, Boolean> getFeatures() {
                return Collections.singletonMap(key, value);
            }
        };
    }
}
