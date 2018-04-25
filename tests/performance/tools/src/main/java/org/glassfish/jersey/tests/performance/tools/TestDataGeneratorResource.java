/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.performance.tools;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Resource for generating data for performance tests.
 * For more information, see {@link TestDataGeneratorApp}
 */
@Path("generate")
public class TestDataGeneratorResource {

    private static Logger LOG = Logger.getLogger(TestDataGeneratorResource.class.getName());

    /**
     * Generates plain text based data from a complex testing bean.
     * @return result of a call to a {@link Object#toString()} method of the populated bean.
     */
    @GET
    @Path("complex/text")
    public String generateComplexText() {
        return getComplexTestBean().toString();
    }

    /**
     * Generates json based data from a complex testing bean.
     * @return the bean to be converted to json
     */
    @GET
    @Path("complex/json")
    @Produces(MediaType.APPLICATION_JSON)
    public TestBean generateComplexJson() {
        return getComplexTestBean();
    }

    /**
     * Generates xml based data from a complex testing bean.
     * @return the bean to be converted to xml
     */
    @GET
    @Path("complex/xml")
    @Produces(MediaType.APPLICATION_XML)
    public TestBean generateComplexXml() {
        return getComplexTestBean();
    }

    /**
     * Generates plain text based data from a simple testing bean.
     * @return result of a call to a {@link Object#toString()} method of the populated bean.
     */
    @GET
    @Path("simple/text")
    public String generateSimpleText() {
        return getSimpleTestBean().toString();
    }

    /**
     * Generates json based data from a simple testing bean.
     * @return the bean to be converted to json
     */
    @GET
    @Path("simple/json")
    @Produces(MediaType.APPLICATION_JSON)
    public Person generateSimpleJson() {
        return getSimpleTestBean();
    }

    /**
     * Generates xml based data from a simple testing bean.
     * @return the bean to be converted to xml
     */
    @GET
    @Path("simple/xml")
    @Produces(MediaType.APPLICATION_XML)
    public Person generateSimpleXml() {
        return getSimpleTestBean();
    }

    private TestBean getComplexTestBean() {
        TestBean bean = new TestBean();
        try {
            TestDataGenerator.populateBeanByAnnotations(bean);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while populating the testing bean.", e);
        }
        return bean;
    }

    private Person getSimpleTestBean() {
        Person bean = new Person();
        try {
            TestDataGenerator.populateBeanByAnnotations(bean);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error while populating the testing bean.", e);
        }
        return bean;
    }
}
