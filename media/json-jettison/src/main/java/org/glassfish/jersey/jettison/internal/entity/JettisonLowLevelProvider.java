/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jettison.internal.entity;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider;

/**
 * Abstract, low-level JSON media type message entity provider (reader & writer).
 *
 * @param <T> supported Java type.
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public abstract class JettisonLowLevelProvider<T> extends AbstractMessageReaderWriterProvider<T> {

    private final Class<T> c;

    // JavaRebel needs this ctor
    protected JettisonLowLevelProvider(Class<T> c) {
        this.c = c;
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return (type == c) && isSupported(mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[], MediaType mediaType) {
        return (type == c) && isSupported(mediaType);
    }

    protected boolean isSupported(MediaType m) {
        return true;
    }
}
