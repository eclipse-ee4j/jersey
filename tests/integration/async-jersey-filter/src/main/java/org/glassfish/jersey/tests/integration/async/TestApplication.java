/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.async;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;

import org.glassfish.jersey.tests.integration.jersey2730.TestExceptionResource;
import org.glassfish.jersey.tests.integration.jersey2730.exception.MappedExceptionMapper;
import org.glassfish.jersey.tests.integration.jersey2812.TestWaitResource;

/**
 * Jersey application for JERSEY-2730.
 *
 * @author Stepan Vavra (stepan.vavra at oracle.com)
 */
@ApplicationPath("/")
public class TestApplication extends ResourceConfig {

    public TestApplication() {
        register(TestExceptionResource.class);
        register(MappedExceptionMapper.class);
        register(TestWaitResource.class);
    }
}
