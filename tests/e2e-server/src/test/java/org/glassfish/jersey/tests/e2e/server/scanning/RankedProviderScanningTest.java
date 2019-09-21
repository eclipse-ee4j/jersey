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

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Michal Gajdos
 */
public class RankedProviderScanningTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig().packages(false, RankedProviderScanningTest.class.getPackage().getName());
    }

    @Test
    public void testRankedProviderScanning() throws Exception {
        WebTarget t = target();
        t.register(LoggingFeature.class);

        Response r = t.path("/").request().get();

        assertEquals(200, r.getStatus());
        assertEquals("get-ext4-ext2-ext1-ext3", r.readEntity(String.class));
    }

}
