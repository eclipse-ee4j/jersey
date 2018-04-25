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

package org.glassfish.jersey.media.multipart.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.server.CloseableService;

import org.jvnet.mimepull.MIMEParsingException;

/**
 * {@link MessageBodyReader} implementation for {@link MultiPart} entities.
 *
 * @author Craig McClanahan
 * @author Paul Sandoz
 * @author Michal Gajdos
 */
@Singleton
@ConstrainedTo(RuntimeType.SERVER)
public class MultiPartReaderServerSide extends MultiPartReaderClientSide {

    private final Provider<CloseableService> closeableServiceProvider;

    @Inject
    public MultiPartReaderServerSide(@Context final Providers providers,
                                     final Provider<CloseableService> closeableServiceProvider) {
        super(providers);
        this.closeableServiceProvider = closeableServiceProvider;
    }

    protected MultiPart readMultiPart(final Class<MultiPart> type,
                                      final Type genericType,
                                      final Annotation[] annotations,
                                      final MediaType mediaType,
                                      final MultivaluedMap<String, String> headers,
                                      final InputStream stream) throws IOException, MIMEParsingException {
        final MultiPart multiPart = super.readMultiPart(type, genericType, annotations, mediaType, headers, stream);
        closeableServiceProvider.get().add(multiPart);
        return multiPart;
    }

}
