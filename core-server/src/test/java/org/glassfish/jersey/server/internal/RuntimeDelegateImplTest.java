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

package org.glassfish.jersey.server.internal;

import javax.ws.rs.core.Application;
import javax.ws.rs.ext.RuntimeDelegate;

import org.junit.Test;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * Unit test that checks that the right RuntimeDelegateImpl is loaded by JAX-RS.
 *
 * @author Martin Matula
 */
public class RuntimeDelegateImplTest {

    @Test
    public void testCreateEndpoint() {
        RuntimeDelegate delegate = RuntimeDelegate.getInstance();
        try {
            delegate.createEndpoint((Application) null, com.sun.net.httpserver.HttpHandler.class);
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException iae) {
            // ok - should be thrown
        } catch (Exception e) {
            fail("IllegalArgumentException should be thrown");
        }
    }

    @Test
    public void testRuntimeDelegateInstance() {
        assertSame(RuntimeDelegateImpl.class, RuntimeDelegate.getInstance().getClass());
    }
}
