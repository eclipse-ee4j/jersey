/*
 * Copyright (c) 2014, 2022 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test injection of request depending instances works as expected.
 *
 * @author Jakub Podlesak
 */
public class RequestSensitiveTest {

    public static Collection<String[]> testData() {
        return Arrays.asList(
                new String[] {"app-field-injected", "alpha", "App: alpha"},
                new String[] {"app-field-injected", "gogol", "App: gogol"},
                new String[] {"app-field-injected", "elcaro", "App: elcaro"},
                new String[] {"app-ctor-injected", "alpha", "App: alpha"},
                new String[] {"app-ctor-injected", "gogol", "App: gogol"},
                new String[] {"app-ctor-injected", "elcaro", "App: elcaro"},
                new String[] {"request-field-injected", "alpha", "Request: alpha"},
                new String[] {"request-field-injected", "gogol", "Request: gogol"},
                new String[] {"request-field-injected", "oracle", "Request: oracle"},
                new String[] {"request-ctor-injected", "alpha", "Request: alpha"},
                new String[] {"request-ctor-injected", "gogol", "Request: gogol"},
                new String[] {"request-ctor-injected", "oracle", "Request: oracle"}
        );
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        for (String[] args : testData()) {
            RequestSensitiveTemplateTest test = new RequestSensitiveTemplateTest(args[0], args[1], args[2]) {};
            tests.add(TestHelper.toTestContainer(test,
                    String.format("%s (%s)", RequestSensitiveTemplateTest.class.getSimpleName(), Arrays.toString(args))));
        }
        return tests;
    }

    public abstract static class RequestSensitiveTemplateTest extends CdiTest {
        String resource;
        String straight;
        String echoed;

        public RequestSensitiveTemplateTest(String resource, String straight, String echoed) {
            this.resource = resource;
            this.straight = straight;
            this.echoed = echoed;
        }

        @Test
        public void testCdiInjection() {
            final String s = target().path(resource).queryParam("s", straight).request().get(String.class);
            assertThat(s, equalTo(echoed));
        }

        @Test
        public void testHk2Injection() {
            final String s = target().path(resource).path("path").path(straight).request().get(String.class);
            assertThat(s, equalTo(String.format("%s/path/%s", resource, straight)));
        }
    }
}
