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

import java.text.ParseException;

/**
 * A token.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class Token {

    protected String token;

    protected Token() {
    }

    public Token(String header) throws ParseException {
        this(HttpHeaderReader.newInstance(header));
    }

    public Token(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();

        token = reader.nextToken().toString();

        if (reader.hasNext()) {
            throw new ParseException("Invalid token", reader.getIndex());
        }
    }

    public String getToken() {
        return token;
    }

    public final boolean isCompatible(String token) {
        if (this.token.equals("*")) {
            return true;
        }

        return this.token.equals(token);
    }
}
