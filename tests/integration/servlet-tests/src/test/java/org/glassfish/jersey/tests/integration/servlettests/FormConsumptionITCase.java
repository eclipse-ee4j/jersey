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

package org.glassfish.jersey.tests.integration.servlettests;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Martin Matula
 */
public class FormConsumptionITCase extends JerseyTest {
    @Override
    protected Application configure() {
        // dummy resource config
        return new ResourceConfig();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Test
    public void testPut() {
        Form form = new Form();
        form.param("text", "this is a test");
        String result = target("form-consumption/form-consumption").request().put(Entity.form(form), String.class);
        assertEquals(form.asMap().getFirst("text"), result);
    }

    @Test
    public void testPost() {
        Form form = new Form();
        form.param("text", "this is a test");
        String result = target("form-consumption/form-consumption").request().post(Entity.form(form), String.class);
        assertEquals(form.asMap().getFirst("text"), result);
    }

    @Test
    public void testPostWithEncoding() {
        Form form = new Form();
        form.param("text", "this is an encoding test +-*/=");
        String result = target("form-consumption/form-consumption/encoding").request().post(Entity.form(form), String.class);
        assertEquals(form.asMap().getFirst("text"), result);
    }
}
