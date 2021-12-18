/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.java.connector;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.spi.AsyncConnectorCallback;
import org.glassfish.jersey.client.spi.Connector;
import org.glassfish.jersey.internal.Version;
import org.glassfish.jersey.message.internal.OutboundMessageContext;
import org.glassfish.jersey.message.internal.Statuses;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.Logger;

/**
 * Provides a Jersey client {@link Connector}, which internally uses Java's {@link HttpClient}.
 * The following properties are provided to Java's {@link HttpClient.Builder} during creation of the {@link HttpClient}:
 * <ul>
 *     <li>{@link ClientProperties#CONNECT_TIMEOUT}</li>
 *     <li>{@link ClientProperties#FOLLOW_REDIRECTS}</li>
 *     <li>{@link JavaClientProperties#COOKIE_HANDLER}</li>
 *     <li>{@link JavaClientProperties#SSL_PARAMETERS}</li>
 * </ul>
 *
 * @author Steffen Nießing
 */
public class JavaConnector implements Connector {
    private static final Logger LOGGER = Logger.getLogger(JavaConnector.class.getName());

    private final HttpClient httpClient;

    /**
     * Constructs a new {@link Connector} for a Jersey client instance using Java's {@link HttpClient}.
     *
     * @param client a Jersey client instance to get additional configuration properties from (e.g. {@link SSLContext})
     * @param configuration the configuration properties for this connector
     */
    public JavaConnector(final Client client, final Configuration configuration) {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        httpClientBuilder.version(HttpClient.Version.HTTP_1_1);
        SSLContext sslContext = client.getSslContext();
        if (sslContext != null) {
            httpClientBuilder.sslContext(sslContext);
        }
        Integer connectTimeout = getPropertyOrNull(configuration, ClientProperties.CONNECT_TIMEOUT, Integer.class);
        if (connectTimeout != null) {
            httpClientBuilder.connectTimeout(Duration.of(connectTimeout, ChronoUnit.MILLIS));
        }
        CookieHandler cookieHandler = getPropertyOrNull(configuration, JavaClientProperties.COOKIE_HANDLER, CookieHandler.class);
        if (cookieHandler != null) {
            httpClientBuilder.cookieHandler(cookieHandler);
        }
        Boolean redirect = getPropertyOrNull(configuration, ClientProperties.FOLLOW_REDIRECTS, Boolean.class);
        if (redirect != null) {
            httpClientBuilder.followRedirects(redirect ? HttpClient.Redirect.ALWAYS : HttpClient.Redirect.NEVER);
        } else {
            httpClientBuilder.followRedirects(HttpClient.Redirect.NORMAL);
        }
        SSLParameters sslParameters = getPropertyOrNull(configuration, JavaClientProperties.SSL_PARAMETERS, SSLParameters.class);
        if (sslParameters != null) {
            httpClientBuilder.sslParameters(sslParameters);
        }
        this.httpClient = httpClientBuilder.build();
    }

    /**
     * Implements a {@link org.glassfish.jersey.message.internal.OutboundMessageContext.StreamProvider}
     * for a {@link ByteArrayOutputStream}.
     */
    private static class ByteArrayOutputStreamProvider implements OutboundMessageContext.StreamProvider {
        private ByteArrayOutputStream byteArrayOutputStream;

        public ByteArrayOutputStream getByteArrayOutputStream() {
            return byteArrayOutputStream;
        }

        @Override
        public OutputStream getOutputStream(int contentLength) throws IOException {
            return this.byteArrayOutputStream = new ByteArrayOutputStream(contentLength);
        }
    }

    /**
     * Builds a request for the {@link HttpClient} from Jersey's {@link ClientRequest}.
     *
     * @param request the Jersey request to get request data from
     * @return the {@link HttpRequest} instance for the {@link HttpClient} request
     */
    private HttpRequest getHttpRequest(ClientRequest request) {
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        builder.uri(request.getUri());
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
        if (request.hasEntity()) {
            try {
                request.enableBuffering();
                ByteArrayOutputStreamProvider byteBufferStreamProvider = new ByteArrayOutputStreamProvider();
                request.setStreamProvider(byteBufferStreamProvider);
                request.writeEntity();
                bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(
                        byteBufferStreamProvider.getByteArrayOutputStream().toByteArray()
                );
            } catch (IOException e) {
                throw new ProcessingException(LocalizationMessages.ERROR_INVALID_ENTITY(), e);
            }
        }
        builder.method(request.getMethod(), bodyPublisher);
        for (Map.Entry<String, List<String>> entry : request.getRequestHeaders().entrySet()) {
            String headerName = entry.getKey();
            for (String headerValue : entry.getValue()) {
                builder.header(headerName, headerValue);
            }
        }
        return builder.build();
    }

    /**
     * Retrieves a property from the configuration, if it was provided.
     *
     * @param configuration the {@link Configuration} to get the property information from
     * @param propertyKey the name of the property to retrieve
     * @param resultClass the type to which the property value should be case
     * @param <T> the generic type parameter of the result type
     * @return the requested property or {@code null}, if it was not provided or has the wrong type
     */
    @SuppressWarnings("unchecked")
    private <T> T getPropertyOrNull(final Configuration configuration, final String propertyKey, final Class<T> resultClass) {
        Object propertyObject = configuration.getProperty(propertyKey);
        if (propertyObject == null) {
            return null;
        }
        if (!resultClass.isInstance(propertyObject)) {
            LOGGER.warning(LocalizationMessages.ERROR_INVALID_CLASS(propertyKey, resultClass.getName()));
            return null;
        }
        return (T) propertyObject;
    }

    /**
     * Translates a {@link HttpResponse} from the {@link HttpClient} to a Jersey {@link ClientResponse}.
     *
     * @param request the {@link ClientRequest} to get additional information (e.g. header values) from
     * @param response the {@link HttpClient} response object
     * @return the translated Jersey {@link ClientResponse} object
     */
    private ClientResponse buildClientResponse(ClientRequest request, HttpResponse<InputStream> response) {
        ClientResponse clientResponse = new ClientResponse(Statuses.from(response.statusCode()), request);
        MultivaluedMap<String, String> headers = clientResponse.getHeaders();
        for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
            String headerName = entry.getKey();
            if (headers.get(headerName) != null) {
                headers.get(headerName).addAll(entry.getValue());
            } else {
                headers.put(headerName, entry.getValue());
            }
        }
        clientResponse.setEntityStream(response.body());
        return clientResponse;
    }

    /**
     * Returns the underlying {@link HttpClient} instance used by this connector.
     *
     * @return the Java {@link HttpClient} instance
     */
    public HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public ClientResponse apply(ClientRequest request) {
        HttpRequest httpRequest = getHttpRequest(request);
        try {
            HttpResponse<InputStream> response = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            return buildClientResponse(request, response);
        } catch (IOException | InterruptedException e) {
            throw new ProcessingException(e);
        }
    }

    @Override
    public Future<?> apply(ClientRequest request, AsyncConnectorCallback callback) {
        HttpRequest httpRequest = getHttpRequest(request);
        CompletableFuture<ClientResponse> response = this.httpClient
                .sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(httpResponse -> buildClientResponse(request, httpResponse));
        response.thenAccept(callback::response);
        return response;
    }

    @Override
    public String getName() {
        return "Java HttpClient Connector " + Version.getVersion();
    }

    @Override
    public void close() {

    }
}
