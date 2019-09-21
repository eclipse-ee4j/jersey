/*
 * Copyright (c) 2018 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.cdi.resources;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

/**
 *
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
public class InheritanceTest extends CdiTest {

    @Test
    public void testGet() {

        final WebTarget target = target().path("user/count");

        final Response response = target.request().get();
        assertThat(response.getStatus(), is(200));

    }
}
