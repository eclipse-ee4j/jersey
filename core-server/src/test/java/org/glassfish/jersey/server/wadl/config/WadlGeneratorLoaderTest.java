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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.jersey.server.wadl.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

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
 * Test the {@link WadlGeneratorLoader}.
 *
 * @author Miroslav Fuksa
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class WadlGeneratorLoaderTest {

    @Test
    public void testLoadFileFromClasspathRelative() throws Exception {
        TestInjectionManagerFactory.BootstrapResult result =
                TestInjectionManagerFactory.createInjectionManager();
        final Properties props = new Properties();
        props.put("testFile", "classpath:testfile.xml");
        final WadlGeneratorDescription description = new WadlGeneratorDescription(MyWadlGenerator2.class, props);

        final WadlGenerator wadlGenerator =
                WadlGeneratorLoader.loadWadlGeneratorDescriptions(result.injectionManager, description);
        Assert.assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());

        final URL resource = getClass().getResource("testfile.xml");
        Assert.assertEquals(new File(resource.toURI()).getAbsolutePath(), ((MyWadlGenerator2) wadlGenerator).getTestFile()
                .getAbsolutePath());

    }

    @Test
    public void testLoadFileFromClasspathAbsolute() throws Exception {
        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();
        final Properties props = new Properties();
        final String path = "classpath:/" + getClass().getPackage().getName().replaceAll("\\.", "/") + "/testfile.xml";
        props.put("testFile", path);
        final WadlGeneratorDescription description = new WadlGeneratorDescription(MyWadlGenerator2.class, props);

        final WadlGenerator wadlGenerator =
                WadlGeneratorLoader.loadWadlGeneratorDescriptions(result.injectionManager, description);
        Assert.assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());

        final URL resource = getClass().getResource("testfile.xml");
        Assert.assertEquals(new File(resource.toURI()).getAbsolutePath(), ((MyWadlGenerator2) wadlGenerator).getTestFile()
                .getAbsolutePath());

    }

    @Test
    public void testLoadFileFromAbsolutePath() throws Exception {
        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();
        final URL resource = getClass().getResource("testfile.xml");

        final Properties props = new Properties();
        final String path = new File(resource.toURI()).getAbsolutePath();
        props.put("testFile", path);
        final WadlGeneratorDescription description = new WadlGeneratorDescription(MyWadlGenerator2.class, props);

        final WadlGenerator wadlGenerator =
                WadlGeneratorLoader.loadWadlGeneratorDescriptions(result.injectionManager, description);
        Assert.assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());

        Assert.assertEquals(new File(resource.toURI()).getAbsolutePath(), ((MyWadlGenerator2) wadlGenerator).getTestFile()
                .getAbsolutePath());
    }

    @Test
    public void testLoadStream() throws Exception {
        TestInjectionManagerFactory.BootstrapResult result = TestInjectionManagerFactory.createInjectionManager();
        final Properties props = new Properties();
        final String path = getClass().getPackage().getName().replaceAll("\\.", "/") + "/testfile.xml";
        props.put("testStream", path);
        final WadlGeneratorDescription description = new WadlGeneratorDescription(MyWadlGenerator2.class, props);

        final WadlGenerator wadlGenerator =
                WadlGeneratorLoader.loadWadlGeneratorDescriptions(result.injectionManager, description);
        Assert.assertEquals(MyWadlGenerator2.class, wadlGenerator.getClass());

        final URL resource = getClass().getResource("testfile.xml");
        Assert.assertEquals(new File(resource.toURI()).length(), ((MyWadlGenerator2) wadlGenerator).getTestStreamContent()
                .length());

    }

    public static class MyWadlGenerator2 implements WadlGenerator {

        private File _testFile;
        private InputStream _testStream;
        private File _testStreamContent;
        private WadlGenerator _delegate;

        /**
         * @param testFile the testFile to set
         */
        public void setTestFile(File testFile) {
            _testFile = testFile;
        }

        public void setTestStream(InputStream testStream) {
            _testStream = testStream;
        }

        public File getTestFile() {
            return _testFile;
        }

        public File getTestStreamContent() {
            /*
            try {
                System.out.println( "listing file " + _testFileContent.getName() );
                BufferedReader in = new BufferedReader( new FileReader( _testFileContent ) );
                String line = null;
                while ( (line = in.readLine()) != null ) {
                    System.out.println( line );
                }
            } catch ( Exception e ) {
                e.printStackTrace();
            }
            */
            return _testStreamContent;
        }

        public void init() throws IOException {
            if (_testStream != null) {
                _testStreamContent = File.createTempFile("testfile-" + getClass().getSimpleName(), null);
                OutputStream to = null;
                try {
                    to = new FileOutputStream(_testStreamContent);
                    byte[] buffer = new byte[4096];
                    int bytes_read;
                    while ((bytes_read = _testStream.read(buffer)) != -1) {
                        to.write(buffer, 0, bytes_read);
                    }
                } finally {
                    // Always close the streams, even if exceptions were thrown
                    if (to != null) {
                        try {
                            to.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        }

        public void setWadlGeneratorDelegate(WadlGenerator delegate) {
            _delegate = delegate;
        }

        /**
         * @return the delegate
         */
        public WadlGenerator getDelegate() {
            return _delegate;
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

        @Override
        public ExternalGrammarDefinition createExternalGrammar() {
            return _delegate.createExternalGrammar();
        }

        @Override
        public void attachTypes(ApplicationDescription egd) {
            _delegate.attachTypes(egd);
        }
    }
}
