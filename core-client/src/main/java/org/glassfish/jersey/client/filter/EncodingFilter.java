/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;

import javax.inject.Inject;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.internal.LocalizationMessages;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.spi.ContentEncoder;

/**
 * Client filter adding support for {@link org.glassfish.jersey.spi.ContentEncoder content encoding}. The filter adds
 * list of supported encodings to the Accept-Header values.
 * Supported encodings are determined by looking
 * up all the {@link org.glassfish.jersey.spi.ContentEncoder} implementations registered in the corresponding
 * {@link ClientConfig client configuration}.
 * <p>
 * If {@link ClientProperties#USE_ENCODING} client property is set, the filter will add Content-Encoding header with
 * the value of the property, unless Content-Encoding header has already been set.
 * </p>
 *
 * @author Martin Matula
 */
public final class EncodingFilter implements ClientRequestFilter {
    @Inject
    private InjectionManager injectionManager;
    private volatile List<Object> supportedEncodings = null;

    @Override
    public void filter(ClientRequestContext request) throws IOException {
        if (getSupportedEncodings().isEmpty()) {
            return;
        }

        request.getHeaders().addAll(HttpHeaders.ACCEPT_ENCODING, getSupportedEncodings());

        String useEncoding = (String) request.getConfiguration().getProperty(ClientProperties.USE_ENCODING);
        if (useEncoding != null) {
            if (!getSupportedEncodings().contains(useEncoding)) {
                Logger.getLogger(getClass().getName()).warning(LocalizationMessages.USE_ENCODING_IGNORED(
                        ClientProperties.USE_ENCODING, useEncoding, getSupportedEncodings()));
            } else {
                if (request.hasEntity()) {   // don't add Content-Encoding header for requests with no entity
                    if (request.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING) == null) {
                        request.getHeaders().putSingle(HttpHeaders.CONTENT_ENCODING, useEncoding);
                    }
                }
            }
        }
    }

    List<Object> getSupportedEncodings() {
        // no need for synchronization - in case of a race condition, the property
        // may be set twice, but it does not break anything
        if (supportedEncodings == null) {
            SortedSet<String> se = new TreeSet<>();
            List<ContentEncoder> encoders = injectionManager.getAllInstances(ContentEncoder.class);
            for (ContentEncoder encoder : encoders) {
                se.addAll(encoder.getSupportedEncodings());
            }
            supportedEncodings = new ArrayList<>(se);
        }
        return supportedEncodings;
    }
}
