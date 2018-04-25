/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.jersey2176;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Libor Kramolis (libor.kramolis at oracle.com)
 */
public class TraceResponseWrapper extends HttpServletResponseWrapper {
    private final ByteArrayOutputStream localStream;

    public TraceResponseWrapper(final HttpServletResponse response) throws IOException {
        super(response);

        localStream = new ByteArrayOutputStream();
        localStream.write("[FILTER]".getBytes(response.getCharacterEncoding()));
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {
            @Override
            public void write(final int b) throws IOException {
                localStream.write(b);
            }
        };
    }

    public void writeBodyAndClose(final String encoding) throws IOException {
        localStream.write("[/FILTER]".getBytes(encoding));

        super.getOutputStream().write(localStream.toByteArray());
        super.getOutputStream().close();
        localStream.close();
    }

    public String getContentLength() {
        return String.valueOf(localStream.size() + "[/FILTER]".length());
    }

}
