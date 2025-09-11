/*
 * Copyright (c) 2021, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.GenericEntity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.innate.spi.EntityPartBuilderProvider;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.glassfish.jersey.media.multipart.internal.l10n.LocalizationMessages;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Jersey implementation of {@link EntityPart.Builder}.
 * @since 3.1.0
 */
public class JerseyEntityPartBuilderProvider implements EntityPartBuilderProvider {

    @Override
    public EntityPart.Builder withName(String partName) {
        return new EnityPartBuilder().withName(partName);
    }

    private static class EnityPartBuilder implements EntityPart.Builder {

        private String partName;
        private String fileName = null;
        private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        private MediaType mediaType = null;
        private MethodData methodData;

        private EntityPart.Builder withName(String partName) {
            this.partName = partName;
            return this;
        }

        @Override
        public EntityPart.Builder mediaType(MediaType mediaType) throws IllegalArgumentException {
            this.mediaType = mediaType;
            return this;
        }

        @Override
        public EntityPart.Builder mediaType(String mediaTypeString) throws IllegalArgumentException {
            this.mediaType = MediaType.valueOf(mediaTypeString);
            return this;
        }

        @Override
        public EntityPart.Builder header(String headerName, String... headerValues) throws IllegalArgumentException {
            this.headers.addAll(headerName, headerValues);
            return this;
        }

        @Override
        public EntityPart.Builder headers(MultivaluedMap<String, String> newHeaders) throws IllegalArgumentException {
            for (Map.Entry<String, List<String>> entry : newHeaders.entrySet()) {
                header(entry.getKey(), entry.getValue().toArray(new String[0]));
            }
            return this;
        }

        @Override
        public EntityPart.Builder fileName(String fileName) throws IllegalArgumentException {
            this.fileName = fileName;
            return this;
        }

        @Override
        public EntityPart.Builder content(InputStream content) throws IllegalArgumentException {
            methodData = new InputStreamMethodData(content);
            return this;
        }

        @Override
        public <T> EntityPart.Builder content(T content, Class<? extends T> type) throws IllegalArgumentException {
            if (File.class.equals(type)) {
                methodData = new FileMethodData((File) content);
            } else if (InputStream.class.equals(type)) {
                methodData = new InputStreamMethodData((InputStream) content);
            } else {
                methodData = new GenericData(content, null);
            }
            return this;
        }

        @Override
        public <T> EntityPart.Builder content(T content, GenericType<T> type) throws IllegalArgumentException {
            if (File.class.equals(type.getRawType())) {
                methodData = new FileMethodData((File) content);
            } else if (InputStream.class.equals(type.getRawType())) {
                methodData = new InputStreamMethodData((InputStream) content);
            } else {
                methodData = new GenericData(content, type);
            }
            return this;
        }

        @Override
        public EntityPart build() throws IllegalStateException, IOException, WebApplicationException {
            if (methodData == null) {
                throw new IllegalStateException(LocalizationMessages.ENTITY_CONTENT_NOT_SET());
            }
            final FormDataBodyPart bodyPart = methodData.build();
            return bodyPart;
        }


        private abstract class MethodData<T> {
            protected MethodData(T content) {
                this.content = content;
            }
            protected final T content;
            protected abstract FormDataBodyPart build();
            protected void fillFormData(FormDataBodyPart bodyPart) {
                FormDataContentDisposition contentDisposition =
                        FormDataContentDisposition.name(partName).fileName(fileName).build();
                bodyPart.setContentDisposition(contentDisposition);
                if (mediaType != null) {
                    bodyPart.setMediaType(mediaType);
                }
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    bodyPart.getHeaders().addAll(entry.getKey(), entry.getValue().toArray(new String[0]));
                }
            }
        }

        private class InputStreamMethodData extends MethodData<InputStream> {
            protected InputStreamMethodData(InputStream content) {
                super(content);
            }

            @Override
            protected FormDataBodyPart build() {
                final StreamDataBodyPart streamDataBodyPart = new StreamDataBodyPart();
                streamDataBodyPart.setFilename(fileName);
                fillFormData(streamDataBodyPart);
                streamDataBodyPart.setStreamEntity(content, mediaType);
                return streamDataBodyPart;
            }
        }

        private class FileMethodData extends MethodData<File> {
            protected FileMethodData(File content) {
                super(content);
            }

            @Override
            protected FormDataBodyPart build() {
                final FileDataBodyPart fileDataBodyPart = new FileDataBodyPart();
                fillFormData(fileDataBodyPart);
                if (mediaType != null) {
                    fileDataBodyPart.setFileEntity(content, mediaType);
                } else {
                    fileDataBodyPart.setFileEntity(content);
                }
                return fileDataBodyPart;
            }
        }

        private class GenericData extends MethodData<Object> {
            private final GenericType<?> genericEntity;

            protected GenericData(Object content, GenericType<?> genericEntity) {
                super(content);
                this.genericEntity = genericEntity;
            }

            @Override
            protected FormDataBodyPart build() {
                final FormDataBodyPart formDataBodyPart = new FormDataBodyPart();
                fillFormData(formDataBodyPart);
                if (genericEntity != null && !GenericEntity.class.isInstance(content)) {
                    GenericEntity entity = new GenericEntity(content, genericEntity.getType());
                    formDataBodyPart.setEntity(entity);
                } else {
                    formDataBodyPart.setEntity(content);
                }

                return formDataBodyPart;
            }
        }
    }
}
