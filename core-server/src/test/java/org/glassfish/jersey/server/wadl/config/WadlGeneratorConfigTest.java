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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.jersey.server.wadl.config;

import java.util.List;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.TestInjectionManagerFactory;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.wadl.WadlGenerator;
import org.glassfish.jersey.server.wadl.internal.ApplicationDescription;

import org.junit.Assert;
import org.junit.Test;

import com.sun.research.ws.wadl.Application;
import com.sun.research.ws.wadl.Method;
import com.sun.research.ws.wadl.Param;
import com.sun.research.ws.wadl.Representation;
import com.sun.research.ws.wadl.Request;
import com.sun.research.ws.wadl.Resource;
import com.sun.research.ws.wadl.Resources;
import com.sun.research.ws.wadl.Response;

/**
 * TODO: DESCRIBE ME<br>
 * Created on: Aug 2, 2008<br>
 *
 * @author <a href="mailto:martin.grotzke@freiheit.com">Martin Grotzke</a>
 * @author Miroslav Fuksa

 */
public class WadlGeneratorConfigTest {

    @Test
    public void testBuildWadlGeneratorFromGenerators() {
        final Class<MyWadlGenerator> generator = MyWadlGenerator.class;
        final Class<MyWadlGenerator2> generator2 = MyWadlGenerator2.class;
        WadlGeneratorConfig config = WadlGeneratorConfig
                .generator(generator)
                .generator(generator2)
                .build();

        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();
        WadlGenerator wadlGenerator = config.createWadlGenerator(result.injectionManager);

        Assert.assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());
        Assert.assertEquals(MyWadlGenerator.class, ((MyWadlGenerator2) wadlGenerator).getDelegate().getClass());
    }

    @Test
    public void testBuildWadlGeneratorFromDescriptions() {
        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();
        final String propValue = "bar";
        WadlGeneratorConfig config = WadlGeneratorConfig.generator(MyWadlGenerator.class)
                .prop("foo", propValue)
                .build();
        WadlGenerator wadlGenerator = config.createWadlGenerator(result.injectionManager);
        Assert.assertEquals(MyWadlGenerator.class, wadlGenerator.getClass());
        Assert.assertEquals(((MyWadlGenerator) wadlGenerator).getFoo(), propValue);

        final String propValue2 = "baz";
        config = WadlGeneratorConfig.generator(MyWadlGenerator.class)
                .prop("foo", propValue).generator(MyWadlGenerator2.class)
                .prop("bar", propValue2)
                .build();
        wadlGenerator = config.createWadlGenerator(result.injectionManager);
        Assert.assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());
        final MyWadlGenerator2 wadlGenerator2 = (MyWadlGenerator2) wadlGenerator;
        Assert.assertEquals(wadlGenerator2.getBar(), propValue2);

        Assert.assertEquals(MyWadlGenerator.class, wadlGenerator2.getDelegate().getClass());
        Assert.assertEquals(((MyWadlGenerator) wadlGenerator2.getDelegate()).getFoo(), propValue);
    }

    @Test
    public void testCustomWadlGeneratorConfig() {

        final String propValue = "someValue";
        final String propValue2 = "baz";
        class MyWadlGeneratorConfig extends WadlGeneratorConfig {

            @Override
            public List<WadlGeneratorDescription> configure() {
                return generator(MyWadlGenerator.class)
                        .prop("foo", propValue)
                        .generator(MyWadlGenerator2.class)
                        .prop("bar", propValue2).descriptions();
            }
        }

        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();

        WadlGeneratorConfig config = new MyWadlGeneratorConfig();
        WadlGenerator wadlGenerator = config.createWadlGenerator(result.injectionManager);

        Assert.assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());
        final MyWadlGenerator2 wadlGenerator2 = (MyWadlGenerator2) wadlGenerator;
        Assert.assertEquals(wadlGenerator2.getBar(), propValue2);

        Assert.assertEquals(MyWadlGenerator.class, wadlGenerator2.getDelegate().getClass());
        Assert.assertEquals(((MyWadlGenerator) wadlGenerator2.getDelegate()).getFoo(), propValue);
    }

    public abstract static class BaseWadlGenerator implements WadlGenerator {

        public Application createApplication() {
            return null;
        }

        public Method createMethod(org.glassfish.jersey.server.model.Resource r, ResourceMethod m) {
            return null;
        }

        public Request createRequest(org.glassfish.jersey.server.model.Resource r,
                                     ResourceMethod m) {
            return null;
        }

        public Param createParam(org.glassfish.jersey.server.model.Resource r,
                                 ResourceMethod m, Parameter p) {
            return null;
        }

        public Representation createRequestRepresentation(
                org.glassfish.jersey.server.model.Resource r, ResourceMethod m,
                MediaType mediaType) {
            return null;
        }

        public Resource createResource(org.glassfish.jersey.server.model.Resource r, String path) {
            return null;
        }

        public Resources createResources() {
            return null;
        }

        public List<Response> createResponses(org.glassfish.jersey.server.model.Resource r,
                                              ResourceMethod m) {
            return null;
        }

        public String getRequiredJaxbContextPath() {
            return null;
        }

        public void init() {
        }

        public void setWadlGeneratorDelegate(WadlGenerator delegate) {
        }

        @Override
        public ExternalGrammarDefinition createExternalGrammar() {
            return new ExternalGrammarDefinition();
        }

        @Override
        public void attachTypes(ApplicationDescription egd) {

        }
    }

    public static class MyWadlGenerator extends BaseWadlGenerator {

        private String _foo;

        /**
         * @return the foo
         */
        public String getFoo() {
            return _foo;
        }

        /**
         * @param foo the foo to set
         */
        public void setFoo(String foo) {
            _foo = foo;
        }

    }

    public static class MyWadlGenerator2 extends BaseWadlGenerator {

        private String _bar;
        private WadlGenerator _delegate;

        /**
         * @return the delegate
         */
        public WadlGenerator getDelegate() {
            return _delegate;
        }

        /**
         * @return the foo
         */
        public String getBar() {
            return _bar;
        }

        /**
         * @param foo the foo to set
         */
        public void setBar(String foo) {
            _bar = foo;
        }

        public void setWadlGeneratorDelegate(WadlGenerator delegate) {
            _delegate = delegate;
        }
    }

    public static class Foo {

        String s;

        public Foo(String s) {
            this.s = s;
        }
    }

    public static class Bar {
    }

    public static class MyWadlGenerator3 extends BaseWadlGenerator {

        Foo foo;
        Bar bar;

        /**
         * @param foo the foo to set
         */
        public void setFoo(Foo foo) {
            this.foo = foo;
        }

        public void setBar(Bar bar) {
            this.bar = bar;
        }
    }

    @Test
    public void testBuildWadlGeneratorFromDescriptionsWithTypes() {
        WadlGeneratorConfig config = WadlGeneratorConfig
                .generator(MyWadlGenerator3.class)
                .prop("foo", "string")
                .prop("bar", new Bar()).build();
        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();
        WadlGenerator wadlGenerator = config.createWadlGenerator(result.injectionManager);

        Assert.assertEquals(MyWadlGenerator3.class, wadlGenerator.getClass());

        MyWadlGenerator3 g = (MyWadlGenerator3) wadlGenerator;
        Assert.assertNotNull(g.foo);
        Assert.assertEquals(g.foo.s, "string");
        Assert.assertNotNull(g.bar);
    }
}
