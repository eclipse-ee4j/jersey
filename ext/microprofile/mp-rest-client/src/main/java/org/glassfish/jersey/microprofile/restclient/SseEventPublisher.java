package org.glassfish.jersey.microprofile.restclient;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.sse.InboundSseEvent;
import org.glassfish.jersey.client.ChunkParser;
import org.glassfish.jersey.client.ChunkedInput;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class SseEventPublisher extends ChunkedInput<InboundEvent> implements Publisher<InboundEvent> {

    private final Executor executor;
    private final Type genericType;
    private final int bufferSize;

    /**
     * Package-private constructor used by the
     * {@link org.glassfish.jersey.microprofile.restclient.SseMessageBodyReader}.
     *
     * @param inputStream response input stream.
     * @param annotations annotations associated with response entity.
     * @param mediaType response entity media type.
     * @param headers response headers.
     * @param messageBodyWorkers message body workers.
     * @param propertiesDelegate properties delegate for this request/response.
     */
    SseEventPublisher(InputStream inputStream,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> headers,
            MessageBodyWorkers messageBodyWorkers,
            PropertiesDelegate propertiesDelegate,
            ExecutorService executor,
            int bufferSize) {
        super(InboundEvent.class, inputStream, annotations, mediaType, headers, messageBodyWorkers, propertiesDelegate);

        super.setParser(SSE_EVENT_PARSER);
        this.executor = executor;
        this.genericType = genericType;
        this.bufferSize = bufferSize;
    }

    /**
     * SSE event chunk parser - SSE chunks are delimited with a fixed "\n\n" and
     * "\r\n\r\n" delimiter in the response stream.
     */
    private static final ChunkParser SSE_EVENT_PARSER = ChunkedInput.createMultiParser("\n\n", "\r\n\r\n");

    private static final Logger LOG = Logger.getLogger(SseEventPublisher.class.getName());

    /**
     * Request {@link SseEventPublisher} to start streaming data.
     *
     * Each {@link SseEventSubscription} will work for only a single
     * {@link Subscriber}. If the {@link SseEventPublisher} rejects the
     * subscription attempt or otherwise fails it will signal the error via
     * {@link Subscriber#onError(Throwable)}.
     *
     * @param subscriber the {@link Subscriber} that will consume signals from
     * the {@link SseEventPublisher}
     */
    @Override
    public void subscribe(Subscriber subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("The subscriber is `null`");
        }
        SseEventSubscription<InboundEvent> subscription = new SseEventSubscription<>(subscriber, bufferSize);
        subscriber.onSubscribe(subscription);
        Runnable readEventTask = () -> {
            Type typeArgument;
            if (genericType instanceof ParameterizedType) {
                typeArgument = ((ParameterizedType) genericType).getActualTypeArguments()[0];
                ChunkedInput<InboundEvent> input = SseEventPublisher.this;
                try {
                    InboundEvent event;
                    //  org.reactivestreams.Publisher<javax.ws.rs.sse.InboundSseEvent>
                    if (typeArgument.equals(InboundSseEvent.class)) {
                        while ((event = input.read()) != null) {
                            subscription.emit(event);
                        }
                    } else {
                        // Read event data as a given Java type e.g org.reactivestreams.Publisher<CustomEvent>
                        while ((event = input.read()) != null) {
                            subscription.emit(event.readData((Class<InboundEvent>) typeArgument));
                        }
                    }
                } catch (Throwable t) {
                    subscription.onError(t);
                    return;
                }
                subscription.onCompletion();
            }
        };
        try {
            executor.execute(readEventTask);
        } catch (RejectedExecutionException e) {
            LOG.log(Level.WARNING, "Executor {0} rejected emit event task", executor);
        }
    }

}
