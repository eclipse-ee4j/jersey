/*
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketException;

import org.junit.jupiter.api.Test;

public class ServerRuntimeTest {

    @Test
    public void ioExceptionInRoot() {
        assertTrue(ServerRuntime.isIOException(new IOException()));
        assertTrue(ServerRuntime.isIOException(new IOException(new RuntimeException())));
    }

    @Test
    public void ioExceptionInCause() {
        assertTrue(ServerRuntime.isIOException(new RuntimeException(new IOException())));
        assertTrue(ServerRuntime.isIOException(new RuntimeException(new RuntimeException(new IOException()))));
    }

    @Test
    public void unckeckedIOExceptionInCause() {
        assertTrue(ServerRuntime.isIOException(new UncheckedIOException(new SocketException())));
        assertTrue(ServerRuntime.isIOException(new RuntimeException(new UncheckedIOException(new SocketException()))));
    }

    @Test
    public void noIOException() {
        assertFalse(ServerRuntime.isIOException(new RuntimeException()));
        assertFalse(ServerRuntime.isIOException(new RuntimeException(new RuntimeException())));
    }

    @Test
    public void nullException() {
        assertFalse(ServerRuntime.isIOException(null));
    }

    @Test
    public void nullCause() {
        assertFalse(ServerRuntime.isIOException(new RuntimeException((Throwable) null)));
    }
}
