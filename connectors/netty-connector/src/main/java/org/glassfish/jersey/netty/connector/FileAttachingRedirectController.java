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

package org.glassfish.jersey.netty.connector;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;

import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;

/**
 * A NettyHttpRedirectController implementation that allows attaching a file only after a redirect.
 * This controller can be configured to hold a file entity that will only be attached to the request
 * after a redirect has occurred.
 *
 * @since 2.47
 */
public class FileAttachingRedirectController extends NettyHttpRedirectController {

    /**
     * Property name for the file entity to be attached after a redirect.
     */
    public static final String FILE_ENTITY_AFTER_REDIRECT
            = "jersey.config.client.netty.file.entity.after.redirect";

    /**
     * Property name for the file entity media type to be used after a redirect.
     */
    public static final String FILE_ENTITY_MEDIA_TYPE_AFTER_REDIRECT
            = "jersey.config.client.netty.file.entity.media.type.after.redirect";

    /**
     * Property name for the file entity annotations to be used after a redirect.
     */
    public static final String FILE_ENTITY_ANNOTATIONS_AFTER_REDIRECT
            = "jersey.config.client.netty.file.entity.annotations.after.redirect";

    @Override
    public boolean prepareRedirect(ClientRequest request, ClientResponse response) {
        boolean result = super.prepareRedirect(request, response);

        if (result) {
            final Object fileEntity = request.getProperty(FILE_ENTITY_AFTER_REDIRECT);
            if (fileEntity != null) {
                final MediaType mediaType = (MediaType) request.getProperty(FILE_ENTITY_MEDIA_TYPE_AFTER_REDIRECT);
                final Annotation[] annotations = (Annotation[]) request.getProperty(FILE_ENTITY_ANNOTATIONS_AFTER_REDIRECT);
                request.setEntity(fileEntity, annotations);
                if (mediaType != null) {
                    request.setMediaType(mediaType);
                }

                request.removeProperty(FILE_ENTITY_AFTER_REDIRECT);
                request.removeProperty(FILE_ENTITY_MEDIA_TYPE_AFTER_REDIRECT);
                request.removeProperty(FILE_ENTITY_ANNOTATIONS_AFTER_REDIRECT);
            }
        }

        return result;
    }
}
