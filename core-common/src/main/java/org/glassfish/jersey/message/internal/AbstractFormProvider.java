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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;


/**
 * Abstract base class for form entity types marshalling & un-marshalling support.
 *
 * @param <T> form type.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class AbstractFormProvider<T> extends AbstractMessageReaderWriterProvider<T> {

    public <M extends MultivaluedMap<String, String>> M readFrom(M map,
                                                                 MediaType mediaType, boolean decode,
                                                                 InputStream entityStream) throws IOException {
        final String encoded = readFromAsString(entityStream, mediaType);

        final String charsetName = ReaderWriter.getCharset(mediaType).name();

        final StringTokenizer tokenizer = new StringTokenizer(encoded, "&");
        String token;
        try {
            while (tokenizer.hasMoreTokens()) {
                token = tokenizer.nextToken();
                int idx = token.indexOf('=');
                if (idx < 0) {
                    map.add(decode ? URLDecoder.decode(token, charsetName) : token, null);
                } else if (idx > 0) {
                    if (decode) {
                        map.add(URLDecoder.decode(token.substring(0, idx), charsetName),
                                URLDecoder.decode(token.substring(idx + 1), charsetName));
                    } else {
                        map.add(token.substring(0, idx), token.substring(idx + 1));
                    }
                }
            }
            return map;
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }
    }

    public <M extends MultivaluedMap<String, String>> void writeTo(
            M t,
            MediaType mediaType,
            OutputStream entityStream) throws IOException {
        final String charsetName = ReaderWriter.getCharset(mediaType).name();

        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> e : t.entrySet()) {
            for (String value : e.getValue()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(URLEncoder.encode(e.getKey(), charsetName));
                if (value != null) {
                    sb.append('=');
                    sb.append(URLEncoder.encode(value, charsetName));
                }
            }
        }

        writeToAsString(sb.toString(), entityStream, mediaType);
    }
}
