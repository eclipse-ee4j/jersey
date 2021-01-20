package org.glassfish.jersey.microprofile.restclient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.Consumes;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Providers;
import org.glassfish.jersey.internal.PropertiesDelegate;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.message.internal.ReaderInterceptorExecutor;
import org.reactivestreams.Publisher;

@Consumes(MediaType.SERVER_SENT_EVENTS)
@ConstrainedTo(RuntimeType.CLIENT)
public class SseMessageBodyReader implements MessageBodyReader<Publisher<InboundEvent>> {

    @Context
    protected Providers providers;

    @Inject
    private javax.inject.Provider<MessageBodyWorkers> messageBodyWorkers;

    @Inject
    private javax.inject.Provider<PropertiesDelegate> propertiesDelegateProvider;

    @Inject
    private javax.inject.Provider<ExecutorService> executorServiceProvider;

    private final int DEFAULT_BUFFER_SIZE = 512;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Publisher.class.isAssignableFrom(type)
                && MediaType.SERVER_SENT_EVENTS_TYPE.isCompatible(mediaType);
    }

    @Override
    public Publisher<InboundEvent> readFrom(Class<Publisher<InboundEvent>> chunkedInputClass,
            Type genericType,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> headers,
            InputStream inputStream) throws IOException, WebApplicationException {
        InputStream closeableInputStream = ReaderInterceptorExecutor.closeableInputStream(inputStream);
        return new SseEventPublisher(
                closeableInputStream,
                genericType,
                annotations,
                mediaType,
                headers,
                messageBodyWorkers.get(),
                propertiesDelegateProvider.get(),
                executorServiceProvider.get(),
                DEFAULT_BUFFER_SIZE
        );
    }
}
