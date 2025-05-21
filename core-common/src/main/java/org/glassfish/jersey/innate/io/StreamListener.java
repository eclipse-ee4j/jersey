/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.innate.io;

import org.glassfish.jersey.message.internal.EntityInputStream;

import java.util.EventListener;

/**
 * Provides possibility to check whether an input stream for an entity is empty or not.
 * <p>
 * Is being used in the {@link EntityInputStream#isEmpty()} check
 * </p>
 */
public interface StreamListener extends EventListener {

    /**
     * Provides information if the underlying stream is empty
     *
     * @return true if the underlying stream is empty
     */
    boolean isEmpty();

    /**
     * Can be used to provide readiness information.
     * <p>
     * If the stream is not ready the calling check in the {@link EntityInputStream#isEmpty()} method will validate
     * the underlying stream as not empty.
     * </p>
     * <p>
     * Throws:
     * IllegalStateException   - if one of the following conditions is true
     *              the associated request is neither upgraded nor the async started
     *              underlying setReadListener is called more than once within the scope of the same request.
     * </p>
     * @return true if the underlying stream is ready.
     */
    boolean isReady();
}
