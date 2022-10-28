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
import java.util.List;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.spi.TestHelper;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test custom HK2 injection.
 *
 * @author Jakub Podlesak
 */
public class CustomInjectionTest {

    public static List<String> testData() {
        return Arrays.asList("app-field-injected", "app-ctor-injected", "request-field-injected", "request-ctor-injected");
    }

    @TestFactory
    public Collection<DynamicContainer> generateTests() {
        Collection<DynamicContainer> tests = new ArrayList<>();
        for (String resource : testData()) {
            CustomInjectionTemplateTest test = new CustomInjectionTemplateTest(resource) {};
            tests.add(TestHelper.toTestContainer(test,
                    String.format("%s (%s)", CustomInjectionTemplateTest.class.getSimpleName(), resource)));
        }
        return tests;
    }

    public abstract static class CustomInjectionTemplateTest extends CdiTest {
        String resource;

        public CustomInjectionTemplateTest(String resource) {
            this.resource = resource;
        }

        /**
         * Check that for one no NPE happens on the server side,
         * and the custom bound instance of {@link CdiInjectedType} gets CDI injected.
         */
        @Test
        public void testCustomHk2Injection1() {
            final WebTarget target = target().path(resource).path("custom");
            final Response response = target.request().get();
            assertThat(response.getStatus(), equalTo(200));
            assertThat(response.readEntity(String.class), equalTo("CDI injected"));
        }

        /**
         * Check that for one no NPE happens on the server side,
         * and the custom bound instance of {@link MyApplication.MyInjection} gets CDI injected.
         */
        @Test
        public void testCustomHk2Injection2(String resource) {
            final WebTarget target = target().path(resource).path("custom2");
            final Response response = target.request().get();
            assertThat(response.getStatus(), equalTo(200));
            assertThat(response.readEntity(String.class), equalTo("CDI would love this"));
        }
    }
}
