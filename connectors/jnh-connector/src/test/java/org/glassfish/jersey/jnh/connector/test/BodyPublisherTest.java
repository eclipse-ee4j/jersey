/*
 * Copyright (c) 2021, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jnh.connector.test;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Checks, that request entities are correctly serialized and deserialized.
 */
public class BodyPublisherTest extends AbstractJavaConnectorTest {
    /**
     * Checks with a simple plain text entity.
     */
    @Test
    public void testStringEntity() {
        Response response = this.requestWithEntity("java-connector/echo", "POST", Entity.text("Echo"));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThatCode(() -> {
            assertThat(response.readEntity(String.class)).isEqualTo("Echo");
        }).doesNotThrowAnyException();
    }

    /**
     * Checks with an octet stream entity.
     */
    @Test
    public void testByteArrayEntity() {
        String test = "test-string";
        Response response = this.requestWithEntity("java-connector/echo-byte-array", "POST",
                Entity.entity(test.getBytes(StandardCharsets.UTF_8), MediaType.APPLICATION_OCTET_STREAM_TYPE));
        assertThat(response.getStatus()).isEqualTo(200);
        assertThatCode(() -> {
            assertThat(response.readEntity(byte[].class))
                    .satisfies(bytes -> assertThat(new String(bytes, StandardCharsets.UTF_8)).isEqualTo("test-string"));
        }).doesNotThrowAnyException();
    }
}
