/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.innate.multipart;

import jakarta.ws.rs.core.EntityPart;

import java.lang.reflect.Type;

/**
 * Jersey extended {@code EntityPart}. Contains arbitrary useful methods.
 *
 * @since 3.1.0
 */
public interface JerseyEntityPart extends EntityPart {
    /**
     * Converts the content stream for this part to the specified class and returns
     * it.
     *
     * Subsequent invocations will result in an {@code IllegalStateException}.
     * Likewise this method will throw an {@code IllegalStateException} if it is called after calling
     * {@link #getContent} or similar {@code getContent} method.
     *
     * @param <T> type parameter of the value returned
     * @param type the {@code Class} that the implementation should convert this
     *             part to
     * @param <T> the entity type
     * @return an instance of the specified {@code Class} representing the content
     *         of this part
     * @throws IllegalStateException    if this method or any of the other
     *                                  {@code getContent} methods has already been
     *                                  invoked
     */
    <T> T getContent(Class<T> type, Type genericType);
}
