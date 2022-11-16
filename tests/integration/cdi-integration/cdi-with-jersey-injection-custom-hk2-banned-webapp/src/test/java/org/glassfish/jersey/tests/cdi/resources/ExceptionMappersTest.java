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

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for exception mapper injection.
 *
 * @author Jakub Podlesak
 */
public class ExceptionMappersTest {

    public static Collection<String> testData() {
        return Arrays.asList("app-field-injected", "app-ctor-injected", "request-field-injected", "request-ctor-injected");
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        for (String resource : testData()) {
            ExceptionMappersTemplateTest test = new ExceptionMappersTemplateTest(resource) {};
            tests.add(TestHelper.toTestContainer(test,
                    String.format("%s (%s)", ExceptionMappersTemplateTest.class.getSimpleName(), resource)));
        }
        return tests;
    }

    public abstract static class ExceptionMappersTemplateTest extends CdiTest {
        String resource;

        public ExceptionMappersTemplateTest(String resource) {
            this.resource = resource;
        }

        /**
         * Check that for one no NPE happens on the server side, and for two
         * the injected mappers remains the same across requests.
         */
        @Test
        public void testMappersNotNull() {
            final WebTarget target = target().path(resource).path("mappers");
            final Response firstResponse = target.request().get();
            assertThat(firstResponse.getStatus(), equalTo(200));
            final String firstValue = firstResponse.readEntity(String.class);
            assertThat(target.request().get(String.class), equalTo(firstValue));
        }
    }
}
