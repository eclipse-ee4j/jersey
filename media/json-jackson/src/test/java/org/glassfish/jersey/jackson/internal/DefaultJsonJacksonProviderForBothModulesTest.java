/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson.internal;

import org.glassfish.jersey.jackson.internal.model.ServiceTest;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Application;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DefaultJsonJacksonProviderForBothModulesTest extends JerseyTest {
    @Override
    protected final Application configure() {
        return new ResourceConfig(ServiceTest.class)
                .property("jersey.config.json.jackson.enabled.modules", "Jdk8Module");
    }

    @Test
    public final void testDisabledModule() {
        final String response = target("entity/simple")
                .request().get(String.class);
        String expected = "{\"name\":\"Hello\",\"value\":\"World\"}";
        List<String> response_list = Arrays.asList(response.replaceAll("[{}]", "").split(","));
        List<String> expected_list = Arrays.asList(expected.replaceAll("[{}]", "").split(","));
        Collections.sort(response_list);

        assertEquals(expected_list, response_list);
    }

}
