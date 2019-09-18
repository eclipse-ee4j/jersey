/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_request_wrapper2;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.tests.integration.servlet_request_wrapper_binding2.RequestResponseInjectedResource;

/**
 * Test for the web.xml configured JAX-RS app.
 *
 * @author Jakub Podlesak
 */
public class WebXmlConfiguredAppRequestResponseTypeITCase extends AbstractRequestResponseTypeTest {

    @Override
    protected String getAppBasePath() {
        return "webxmlconfigured";
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(RequestResponseInjectedResource.class);
    }
}
