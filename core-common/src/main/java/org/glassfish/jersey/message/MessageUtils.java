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

package org.glassfish.jersey.message;

import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.message.internal.ReaderWriter;

/**
 * Utility class with message related methods.
 *
 * @author Adam Lindenthal (adam.lindenthal at oracle.com)
 */
public final class MessageUtils {

    /**
     * Get the character set from a media type.
     * <p>
     * The character set is obtained from the media type parameter "charset".
     * If the parameter is not present the {@code UTF8} charset is utilized.
     *
     * @param media the media type.
     * @return the character set.
     */
    public static Charset getCharset(final MediaType media) {
        return ReaderWriter.getCharset(media);
    }

    /**
     * Prevent instantiation.
     */
    private MessageUtils() {
        throw new AssertionError("No instances allowed.");
    }
}
