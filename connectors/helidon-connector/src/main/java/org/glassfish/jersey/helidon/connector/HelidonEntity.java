/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.helidon.connector;

import io.helidon.common.GenericType;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.MediaType;
import io.helidon.common.reactive.Multi;
import io.helidon.common.reactive.OutputStreamPublisher;
import io.helidon.common.reactive.Single;
import io.helidon.media.common.ByteChannelBodyWriter;
import io.helidon.media.common.ContentWriters;
import io.helidon.media.common.MessageBodyContext;
import io.helidon.media.common.MessageBodyWriter;
import io.helidon.media.common.MessageBodyWriterContext;
import io.helidon.webclient.WebClientRequestBuilder;
import io.helidon.webclient.WebClientResponse;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;

import javax.ws.rs.ProcessingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.function.Function;

/**
 * A utility class that converts outbound client entity to a class understandable by Helidon.
 * Based on the {@link HelidonEntityType} an entity writer is provided to be registered by Helidon client
 * and an Entity is provided to be submitted by the Helidon Client.
 */
class HelidonEntity {
    /**
     * HelidonEnity type chosen by HelidonEntityType
     */
    enum HelidonEntityType {
        /**
         * Simplest structure. Loads all data to the memory.
         */
        BYTE_ARRAY_OUTPUT_STREAM,
        /**
         * Readable ByteChannel that is capable of sending data in chunks.
         * Capable of caching of bytes before the data are consumed by Helidon.
         */
        READABLE_BYTE_CHANNEL,
        /**
         * Helidon most native entity. Could be slower than {@link #READABLE_BYTE_CHANNEL}.
         */
        OUTPUT_STREAM_PUBLISHER
    }

    /**
     * Get optional entity writer to be registered by the Helidon Client. For some default providers,
     * nothing is needed to be registered.
     * @param type the type of the entity class that works best for the Http Client request use case.
     * @return possible writer to be registerd by the Helidon Client.
     */
    static Optional<MessageBodyWriter<?>> helidonWriter(HelidonEntityType type) {
        switch (type) {
            case BYTE_ARRAY_OUTPUT_STREAM:
                return Optional.of(new OutputStreamBodyWriter());
            case OUTPUT_STREAM_PUBLISHER:
                //Helidon default
                return Optional.empty();
            case READABLE_BYTE_CHANNEL:
                return Optional.of(ByteChannelBodyWriter.create());
        }
        return Optional.empty();
    }

    /**
     * Convert Jersey {@code OutputStream} to an entity based on the client request use case and submits to the provided
     * {@code WebClientRequestBuilder}.
     * @param type the type of the Helidon entity.
     * @param requestContext Jersey {@link ClientRequest} providing the entity {@code OutputStream}.
     * @param requestBuilder Helidon {@code WebClientRequestBuilder} which is used to submit the entity
     * @param executorService {@link ExecutorService} that fills the entity instance for Helidon with data from Jersey
     *                      {@code OutputStream}.
     * @return Helidon Client response completion stage.
     */
    static CompletionStage<WebClientResponse> submit(HelidonEntityType type,
                                                     ClientRequest requestContext,
                                                     WebClientRequestBuilder requestBuilder,
                                                     ExecutorService executorService) {
        CompletionStage<WebClientResponse> stage = null;
        if (type != null) {
            final int bufferSize = requestContext.resolveProperty(
                    ClientProperties.OUTBOUND_CONTENT_LENGTH_BUFFER, 8192);
            switch (type) {
                case BYTE_ARRAY_OUTPUT_STREAM:
                    final ByteArrayOutputStream baos = new ByteArrayOutputStream(bufferSize);
                    requestContext.setStreamProvider(contentLength -> baos);
                    ((ProcessingRunnable) () -> requestContext.writeEntity()).run();
                    stage = requestBuilder.submit(baos);
                    break;
                case READABLE_BYTE_CHANNEL:
                    final OutputStreamChannel channel = new OutputStreamChannel(bufferSize);
                    requestContext.setStreamProvider(contentLength -> channel);
                    executorService.execute((ProcessingRunnable) () -> requestContext.writeEntity());
                    stage = requestBuilder.submit(channel);
                    break;
                case OUTPUT_STREAM_PUBLISHER:
                    final OutputStreamPublisher publisher = new OutputStreamPublisher();
                    requestContext.setStreamProvider(contentLength -> publisher);
                    executorService.execute((ProcessingRunnable) () -> {
                        requestContext.writeEntity();
                        publisher.close();
                    });
                    stage = requestBuilder.submit(Multi.from(publisher).map(DataChunk::create));
                    break;
            }
        }
        return stage;
    }

    @FunctionalInterface
    private interface ProcessingRunnable extends Runnable {
        void runOrThrow() throws IOException;

        @Override
        default void run() {
            try {
                runOrThrow();
            } catch (IOException e) {
                throw new ProcessingException(LocalizationMessages.ERROR_WRITING_ENTITY(e.getMessage()), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static class OutputStreamBodyWriter implements MessageBodyWriter {
        private OutputStreamBodyWriter() {
        }

        @Override
        public Flow.Publisher<DataChunk> write(Single content, GenericType type, MessageBodyWriterContext context) {
            context.contentType(MediaType.APPLICATION_OCTET_STREAM);
            return content.flatMap(new ByteArrayOutputStreamToChunks());
        }

        @Override
        public boolean accept(GenericType type, MessageBodyContext context) {
            return ByteArrayOutputStream.class.isAssignableFrom(type.rawType());
        }

        private static class ByteArrayOutputStreamToChunks implements Function<ByteArrayOutputStream, Flow.Publisher<DataChunk>> {
            @Override
            public Flow.Publisher<DataChunk> apply(ByteArrayOutputStream byteArrayOutputStream) {
                return ContentWriters.writeBytes(byteArrayOutputStream.toByteArray(), false);
            }
        }
    }
}
