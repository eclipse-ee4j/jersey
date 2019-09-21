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

package org.glassfish.jersey.message.internal;

import javax.ws.rs.ext.MessageBodyReader;

/**
 * A {@link MessageBodyReader} may implement this interface to signal that
 * reading from the underlying input stream can be fully completed.
 * <p>
 * This is useful in scenarios where an instance of the type will be processed
 * after the input stream, from which it was read, has been closed.
 *
 * @param <T> the type returned from a {@link MessageBodyReader}.
 *
 * @author Paul Sandoz
 * @see MessageBodyReader
 */
public interface CompletableReader<T> {

    /**
     * Complete the reading.
     *
     * @param t an instance of the Type {@code T}.
     * @return the complete instance of {@code T}.
     */
    T complete(T t);
}
