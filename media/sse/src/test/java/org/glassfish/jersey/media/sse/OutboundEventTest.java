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

package org.glassfish.jersey.media.sse;

import java.util.ArrayList;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.util.ReflectionHelper;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Basic set of unit tests for OutboundEvent creation.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class OutboundEventTest {
    @Test
    public void testGetCommonFields() throws Exception {
        OutboundEvent event;

        event = new OutboundEvent.Builder().id("id").name("name").data("data").build();
        assertEquals("id", event.getId());
        assertEquals("name", event.getName());
        assertEquals("data", event.getData());
        assertEquals(MediaType.TEXT_PLAIN_TYPE, event.getMediaType());
        assertEquals(SseFeature.RECONNECT_NOT_SET, event.getReconnectDelay());
        assertFalse(event.isReconnectDelaySet());

        event = new OutboundEvent.Builder().mediaType(MediaType.APPLICATION_JSON_TYPE).data("data").build();
        assertEquals(MediaType.APPLICATION_JSON_TYPE, event.getMediaType());
        try {
            new OutboundEvent.Builder().mediaType(null);
            fail("NullPointerException expected when setting null mediaType.");
        } catch (NullPointerException ex) {
            // success
        }

        event = new OutboundEvent.Builder().reconnectDelay(-1000).data("data").build();
        assertEquals(SseFeature.RECONNECT_NOT_SET, event.getReconnectDelay());
        assertFalse(event.isReconnectDelaySet());

        event = new OutboundEvent.Builder().reconnectDelay(1000).data("data").build();
        assertEquals(1000, event.getReconnectDelay());
        assertTrue(event.isReconnectDelaySet());
    }

    @Test
    public void testGetCommentOrData() throws Exception {
        assertEquals("comment", new OutboundEvent.Builder().comment("comment").build().getComment());

        assertEquals("data", new OutboundEvent.Builder().data("data").build().getData());

        try {
            new OutboundEvent.Builder().data(null);
            fail("NullPointerException expected when setting null data or data type.");
        } catch (NullPointerException ex) {
            // success
        }
        try {
            new OutboundEvent.Builder().data((Class) null, null);
            fail("NullPointerException expected when setting null data or data type.");
        } catch (NullPointerException ex) {
            // success
        }
        try {
            new OutboundEvent.Builder().data((GenericType) null, null);
            fail("NullPointerException expected when setting null data or data type.");
        } catch (NullPointerException ex) {
            // success
        }

        try {
            new OutboundEvent.Builder().build();
            fail("IllegalStateException when building event with no comment or data.");
        } catch (IllegalStateException ex) {
            // success
        }
    }

    @Test
    public void testDataType() throws Exception {
        OutboundEvent event;

        event = new OutboundEvent.Builder().data("data").build();
        assertEquals(String.class, event.getType());
        assertEquals(String.class, event.getGenericType());

        final GenericEntity<ArrayList<String>> data = new GenericEntity<ArrayList<String>>(new ArrayList<String>()) {
        };
        event = new OutboundEvent.Builder().data(data).build();
        assertEquals(ArrayList.class, event.getType());
        assertEquals(ArrayList.class, ReflectionHelper.erasure(event.getGenericType()));
        assertEquals(data.getType(), event.getGenericType());


        // data part set to an arbitrary instance as it is irrelevant for the test
        event = new OutboundEvent.Builder().data(Integer.class, "data").build();
        assertEquals(Integer.class, event.getType());
        assertEquals(Integer.class, event.getGenericType());

        // data part set to an arbitrary instance as it is irrelevant for the test
        event = new OutboundEvent.Builder().data(new GenericType<ArrayList<String>>() {
        }, "data").build();
        assertEquals(ArrayList.class, event.getType());
        assertEquals(ArrayList.class, ReflectionHelper.erasure(event.getGenericType()));
        assertEquals(new GenericEntity<ArrayList<String>>(new ArrayList<String>()) {
        }.getType(), event.getGenericType());
    }
}
