/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * Portions contributed by Joseph Walton (Atlassian)
 */

package org.glassfish.jersey.jaxb.internal;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.xml.parsers.SAXParserFactory;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Binder;
import org.glassfish.jersey.internal.inject.CompositeBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.inject.PerThread;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * @author Martin Matula
 */
public class SaxParserFactoryInjectionProviderTest {
    private InjectionManager injectionManager;
    private SAXParserFactory f1;
    private SAXParserFactory f2;
    private SAXParserFactory ff1;
    private SAXParserFactory ff2;

    @Before
    public void setUp() {
        injectionManager = createInjectionManager();
    }

    private static final Configuration EMPTY_CONFIG = new Configuration() {

        @Override
        public RuntimeType getRuntimeType() {
            return null;
        }

        @Override
        public Object getProperty(String propertyName) {
            return null;
        }

        @Override
        public Collection<String> getPropertyNames() {
            return Collections.emptyList();
        }

        @Override
        public boolean isEnabled(Feature feature) {
            return false;
        }

        @Override
        public boolean isEnabled(Class<? extends Feature> featureClass) {
            return false;
        }

        @Override
        public boolean isRegistered(Object provider) {
            return false;
        }

        @Override
        public boolean isRegistered(Class<?> providerClass) {
            return false;
        }

        @Override
        public Map<String, Object> getProperties() {
            return Collections.emptyMap();
        }

        @Override
        public Map<Class<?>, Integer> getContracts(Class<?> componentClass) {
            return Collections.emptyMap();
        }

        @Override
        public Set<Class<?>> getClasses() {
            return Collections.emptySet();
        }

        @Override
        public Set<Object> getInstances() {
            return Collections.emptySet();
        }
    };

    public static InjectionManager createInjectionManager(Binder... customBinders) {
        Binder[] binders = new Binder[customBinders.length + 2];

        binders[0] = new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(() -> EMPTY_CONFIG).to(Configuration.class);
                bindFactory(SaxParserFactoryInjectionProvider.class, Singleton.class)
                        .to(SAXParserFactory.class)
                        .in(PerThread.class);
                bindAsContract(MySPFProvider.class).in(Singleton.class);
            }
        };
        System.arraycopy(customBinders, 0, binders, 2, customBinders.length);
        InjectionManager injectionManager = Injections.createInjectionManager(CompositeBinder.wrap(binders));
        injectionManager.completeRegistration();
        return injectionManager;
    }

    @Test
    public void xmlReaderDoesNotResolveExternalParameterEntities() throws Exception {
        String url = "file:///no-such-file";
        String content = "<!DOCTYPE x [<!ENTITY % pe SYSTEM '" + url + "'> %pe;]><x/>";
        getSPF().newSAXParser().getXMLReader().parse(new InputSource(new ByteArrayInputStream(content.getBytes("us-ascii"))));
    }

    /**
     * Making sure that the same instance of SAXParserFactory is used if injected multiple times in the same thread.
     */
    @Test
    public void testSameForSameThreads() {
        f1 = getSPF();
        f2 = getSPF();
        ff1 = getSPFViaProvider();
        ff2 = getSPFViaProvider();
        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(ff1);
        assertNotNull(ff2);

//        System.out.println("f1  : " + f1.toString());
//        System.out.println("ff1 : " + ff1.toString());
//        System.out.println("f2  : " + f2.toString());
//        System.out.println("ff2 : " + ff2.toString());

        assertSame(f1, f2);
        assertSame(f2, ff1);
        assertSame(ff1, ff2);
    }

    /**
     * Making sure that a different instance of SAXParserFactory is used for each different thread.
     * @throws InterruptedException
     */
    @Test
    public void testDifferentForDifferentThreads() throws InterruptedException {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                f1 = getSPF();
                ff1 = getSPFViaProvider();
            }
        });
        t.start();
        f2 = getSPF();
        ff2 = getSPFViaProvider();
        t.join();
        assertNotNull(f1);
        assertNotNull(f2);
        assertNotNull(ff1);
        assertNotNull(ff2);

//        System.out.println("f1  : " + f1.toString());
//        System.out.println("ff1 : " + ff1.toString());
//        System.out.println("f2  : " + f2.toString());
//        System.out.println("ff2 : " + ff2.toString());

        assertNotSame(f1, f2);
        assertNotSame(ff1, ff2);
        assertSame(f1, ff1);
        assertSame(f2, ff2);
    }

    private SAXParserFactory getSPF() {
        return injectionManager.getInstance(SAXParserFactory.class);
    }

    private SAXParserFactory getSPFViaProvider() {
        return injectionManager.<MySPFProvider>getInstance(MySPFProvider.class).getSPF();
    }

    /**
     * Class to emulate injecting a Factory&lt;SAXParserFactory&gt;
     */
    public static class MySPFProvider {
        private final Provider<SAXParserFactory> f;

        @Inject
        public MySPFProvider(Provider<SAXParserFactory> f) {
            this.f = f;
        }

        public SAXParserFactory getSPF() {
            return f.get();
        }
    }
}
