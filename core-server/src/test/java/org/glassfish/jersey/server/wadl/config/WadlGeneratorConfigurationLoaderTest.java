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

package org.glassfish.jersey.server.wadl.config;

import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
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
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 * @author Miroslav Fuksa
 */
public class WadlGeneratorConfigurationLoaderTest {


    @Test
    public void testLoadConfigClass() throws URISyntaxException {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.WADL_GENERATOR_CONFIG,
                MyWadlGeneratorConfig.class.getName());

        TestInjectionManagerFactory.BootstrapResult result =
                TestInjectionManagerFactory.createInjectionManager(resourceConfig);
        final WadlGenerator wadlGenerator = WadlGeneratorConfigLoader.loadWadlGeneratorsFromConfig(resourceConfig.getProperties())
                .createWadlGenerator(result.injectionManager);
        Assert.assertEquals(MyWadlGenerator.class, wadlGenerator.getClass());

    }

    @Test
    public void testLoadConfigInstance() {
        final WadlGeneratorConfig config = WadlGeneratorConfig.generator(MyWadlGenerator.class).build();

        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.property(ServerProperties.WADL_GENERATOR_CONFIG, config);
        TestInjectionManagerFactory.BootstrapResult result =
                TestInjectionManagerFactory.createInjectionManager(resourceConfig);
        final WadlGenerator wadlGenerator = WadlGeneratorConfigLoader.loadWadlGeneratorsFromConfig(resourceConfig.getProperties())
                .createWadlGenerator(result.injectionManager);
        Assert.assertTrue(wadlGenerator instanceof MyWadlGenerator);
    }

    public static class MyWadlGenerator implements WadlGenerator {

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
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void attachTypes(ApplicationDescription egd) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

    }


    public static class MyWadlGeneratorConfig extends WadlGeneratorConfig {

        @Override
        public List<WadlGeneratorDescription> configure() {
            return generator(MyWadlGenerator.class)
                    .prop("foo", "bar")
                    .descriptions();
        }
    }

}
