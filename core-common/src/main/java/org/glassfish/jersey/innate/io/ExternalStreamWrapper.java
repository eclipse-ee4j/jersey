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

import java.io.InputStream;

/**
 * Wrapper for streams from external sources.
 * Could be used in connectors to help determine the stream's emptiness.
 */

public interface ExternalStreamWrapper {

    /**
     * Provides a listener for the underlying input stream. The listener can reflect a particular state which
     * helps to determine whether the underlying stream is empty or not.
     *
     * @return listener
     */
    ExternalStreamListener getListener();

    /**
     * Provides underlying input stream.
     * @return underlying input stream
     */
    InputStream getExternalStream();

}
