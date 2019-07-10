/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_init_provider;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Libor Kramolis
 */
public class HelloWorld5ResourceITCase extends AbstractHelloWorldResourceTest {

    protected Class<?> getResourceClass() {
        return HelloWorld5Resource.class;
    }

    protected int getIndex() {
        return 5;
    }

    @Test
    public void testRegisterFilter() throws Exception {
        String actual = target("application" + getIndex()).path("helloworld" + getIndex()).path("filter").request()
                .get(String.class);
        Assert.assertEquals(TestServletContainerProvider.TEST_FILTER, actual);
    }

}
