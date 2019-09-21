/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.server.validation.validateonexecution;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.executable.ValidateOnExecution;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Testing whether an {@link javax.validation.ValidationException} is raised when {@link ValidateOnExecution} is present on
 * overriding/implementing method as well.
 *
 * @author Michal Gajdos
 */
public class ValidateOnExecutionOverrideTest extends JerseyTest {

    public static interface Validation {

        @NotNull
        @ValidateOnExecution
        public String interfaceMessage();
    }

    public abstract static class ValidationBase {

        @NotNull
        @ValidateOnExecution
        public abstract String classMessage();
    }

    @Path("/")
    public static class ValidationResource extends ValidationBase implements Validation {

        @GET
        @Path("interface")
        @ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
        public String interfaceMessage() {
            return "ko";
        }

        @GET
        @Path("class")
        @ValidateOnExecution(type = ExecutableType.GETTER_METHODS)
        public String classMessage() {
            return "ko";
        }
    }

    @Override
    protected Application configure() {
        enable(TestProperties.DUMP_ENTITY);
        enable(TestProperties.LOG_TRAFFIC);

        set(TestProperties.RECORD_LOG_LEVEL, Level.WARNING.intValue());

        return new ResourceConfig(ValidationResource.class);
    }

    @Test
    public void testOverridingCheckOnInterface() throws Exception {
        _test("interface");
    }

    @Test
    public void testOverridingCheckOnClass() throws Exception {
        _test("class");
    }

    private void _test(final String path) throws Exception {
        assertThat(target(path).request().get().getStatus(), equalTo(500));

        final List<LogRecord> loggedRecords = getLoggedRecords();
        assertThat(loggedRecords.size(), equalTo(1));
        assertThat(loggedRecords.get(0).getThrown(), instanceOf(ValidationException.class));
    }
}
