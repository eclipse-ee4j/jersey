/*
 * Copyright (c) 2019 Christian Kaltepoth. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey4099;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.jboss.weld.environment.se.Weld;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class PriorityAnnotationOnExceptionMappersTest extends JerseyTest {

    private Weld weld;

    @Override
    public void setUp() throws Exception {
        weld = new Weld();
        weld.initialize();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        weld.shutdown();
    }

    @Override
    protected ResourceConfig configure() {
        return ResourceConfig.forApplicationClass(MyApplication.class);
    }

    @Test
    public void testCorrectMapperSelectedAccordingToPriorityAnnotation() {

        assertThat(
                target("/exception").request().get(String.class),
                containsString("MyPriority100Mapper")  // Prio 100 mapper should win
        );

    }

}
