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

package org.glassfish.jersey.tests.e2e.server.scanning;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;

import javax.inject.Inject;

import org.glassfish.jersey.tests.e2e.server.scanning.ext.Ext1WriterInterceptor;
import org.glassfish.jersey.tests.e2e.server.scanning.ext.Ext2WriterInterceptor;
import org.glassfish.jersey.tests.e2e.server.scanning.ext.Ext3WriterInterceptor;
import org.glassfish.jersey.tests.e2e.server.scanning.ext.Ext4WriterInterceptor;

import static org.junit.Assert.assertTrue;

/**
* @author Michal Gajdos
*/
@Path("/")
public class Resource {

    private final Configuration config;

    @Inject
    public Resource(final Configuration config) {
        this.config = config;
    }

    @GET
    public String get() {
        assertTrue(config.getClasses().size() >= 4); // e.g. WADL resource can be there too.
        assertTrue(config.isRegistered(CustomFeature.class));
        assertTrue(config.isRegistered(Ext2WriterInterceptor.class));
        assertTrue(config.isRegistered(Ext3WriterInterceptor.class));
        assertTrue(config.isRegistered(Resource.class));
        assertTrue(config.getClasses().contains(CustomFeature.class));
        assertTrue(config.getClasses().contains(Ext2WriterInterceptor.class));
        assertTrue(config.getClasses().contains(Ext3WriterInterceptor.class));
        assertTrue(config.getClasses().contains(Resource.class));

        assertTrue(config.getInstances().size() >= 2);
        assertTrue(config.getInstances().contains(Ext1WriterInterceptor.INSTANCE));
        assertTrue(config.getInstances().contains(Ext4WriterInterceptor.INSTANCE));
        assertTrue(config.isRegistered(Ext1WriterInterceptor.class));
        assertTrue(config.isRegistered(Ext4WriterInterceptor.class));

        assertTrue(config.isEnabled(CustomFeature.class));

        return "get";
    }
}
