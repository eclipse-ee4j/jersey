/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.glassfish.jersey.tests.e2e.tls.explorer;

import java.nio.ByteBuffer;
import java.util.List;
import javax.net.ssl.SNIServerName;

/**
 * Encapsulates the security capabilities of an SSL/TLS connection.
 * <P>
 * The security capabilities are the list of ciphersuites to be accepted in
 * an SSL/TLS handshake, the record version, the hello version, and server
 * name indication, etc., of an SSL/TLS connection.
 * <P>
 * <code>SSLCapabilities</code> can be retrieved by exploring the network
 * data of an SSL/TLS connection via {@link SSLExplorer#explore(ByteBuffer)}
 * or {@link SSLExplorer#explore(byte[], int, int)}.
 *
 * @see SSLExplorer
 */
public abstract class SSLCapabilities {

    /**
     * Returns the record version of an SSL/TLS connection
     *
     * @return a non-null record version
     */
    public abstract String getRecordVersion();

    /**
     * Returns the hello version of an SSL/TLS connection
     *
     * @return a non-null hello version
     */
    public abstract String getHelloVersion();

    /**
     * Returns a <code>List</code> containing all {@link SNIServerName}s
     * of the server name indication.
     *
     * @return a non-null immutable list of {@link SNIServerName}s
     *         of the server name indication parameter, may be empty
     *         if no server name indication.
     *
     * @see SNIServerName
     */
    public abstract List<SNIServerName> getServerNames();
}

