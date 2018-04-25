/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Collections;

import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Injections;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.MessageBodyFactory;
import org.glassfish.jersey.message.internal.MessagingBinders;
import org.glassfish.jersey.model.internal.CommonConfig;
import org.glassfish.jersey.model.internal.ComponentBag;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Petr Bouda
 */
public class InboundEventReaderTest {

    private static final MultivaluedStringMap HEADERS;

    private InjectionManager injectionManager;

    static {
        HEADERS = new MultivaluedStringMap();
        HEADERS.put("Transfer-Encoding", Collections.singletonList("chunked"));
        HEADERS.put("Content-Type", Collections.singletonList("text/event-stream"));
    }

    @Before
    public void setup() {
        injectionManager = Injections.createInjectionManager();
        injectionManager.register(new TestBinder());

        MessageBodyFactory messageBodyFactory =
                new MessageBodyFactory(new CommonConfig(RuntimeType.SERVER, ComponentBag.EXCLUDE_EMPTY));

        injectionManager.register(Bindings.service(messageBodyFactory).to(MessageBodyWorkers.class));
        injectionManager.completeRegistration();

        messageBodyFactory.initialize(injectionManager);
    }

    @Test
    public void testReadWithStartsWithLF() throws Exception {
        InboundEvent event = parse(new ByteArrayInputStream("\nevent: custom-message".getBytes()));
        assertEquals("custom-message", event.getName());
        assertEquals(0, event.getRawData().length);
    }

    @Test
    public void testReadWithStartsWithCR() throws Exception {
        InboundEvent event = parse(new ByteArrayInputStream("\revent: custom-message".getBytes()));
        assertEquals("custom-message", event.getName());
        assertEquals(0, event.getRawData().length);
    }

    @Test
    public void testReadWithLF() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("event: custom-message\ndata: message 1".getBytes());
        InboundEvent event = parse(inputStream);
        assertEquals("custom-message", event.getName());
        assertDataEquals("message 1", event);
    }

    @Test
    public void testReadWithCRLF() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("event: custom-message\r\ndata: message 1".getBytes());
        InboundEvent event = parse(inputStream);
        assertEquals("custom-message", event.getName());
        assertDataEquals("message 1", event);
    }

    @Test
    public void testReadWithCR() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("event: custom-message\rdata: message 1".getBytes());
        InboundEvent event = parse(inputStream);
        assertEquals("custom-message", event.getName());
        assertDataEquals("message 1", event);
    }

    @Test
    public void testReadWithMultipleSpaces() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("event:     custom-message\rdata:   message 1".getBytes());
        InboundEvent event = parse(inputStream);
        assertEquals("custom-message", event.getName());
        assertDataEquals("message 1", event);
    }

    @Test
    public void testReadWithMultipleEndingDelimiter() throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("event: custom-message\rdata: message 1\r".getBytes());
        InboundEvent event = parse(inputStream);
        assertEquals("custom-message", event.getName());
        assertDataEquals("message 1", event);
    }

    private InboundEvent parse(InputStream stream) throws IOException {
        return injectionManager.getInstance(InboundEventReader.class)
                .readFrom(InboundEvent.class, InboundEvent.class, new Annotation[0],
                MediaType.valueOf(SseFeature.SERVER_SENT_EVENTS), HEADERS, stream);
    }

    private void assertDataEquals(final String expectedData, final InboundEvent event) {
        assertEquals(expectedData, event.readData());
        assertEquals(expectedData, new String(event.getRawData(), Charset.defaultCharset()));
    }

    private static class TestBinder extends AbstractBinder {

        @Override
        protected void configure() {
            install(new MessagingBinders.MessageBodyProviders(null, RuntimeType.SERVER));
            bindAsContract(InboundEventReader.class);
        }
    }
}
