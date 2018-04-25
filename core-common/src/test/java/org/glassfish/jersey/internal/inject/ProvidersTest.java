/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.inject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import org.glassfish.jersey.spi.Contract;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link Providers}.
 *
 * @author Miroslav Fuksa
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ProvidersTest {

    @Test
    public void testIsProviderInterface() {
        assertEquals(true, Providers.isProvider(Provider.class));
        assertEquals(false, Providers.isProvider(NotProvider.class));
        assertEquals(true, Providers.isProvider(JaxRsProvider.class));
        assertEquals(true, Providers.isProvider(ClassBasedProvider.class));
    }

    public static interface NonContractInterface {
    }

    @Contract
    public static interface ContractInterface {
    }

    @Contract
    public abstract static class ContractClass {
    }

    public static class Provider implements ContractInterface {
    }

    public static class NotProvider implements NonContractInterface {
    }

    public static class JaxRsProvider implements MessageBodyReader<String> {

        @Override
        public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return false;
        }

        @Override
        public String readFrom(Class<String> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                               MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException,
                WebApplicationException {
            return null;
        }
    }

    public static class ClassBasedProvider extends ContractClass {
    }

}
