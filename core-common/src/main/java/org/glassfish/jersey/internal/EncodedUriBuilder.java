package org.glassfish.jersey.internal;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import static java.util.stream.Stream.of;

public class EncodedUriBuilder extends UriBuilder {
    private final UriBuilder delegate;
    private final ParameterMarshaller marshaller;

    public EncodedUriBuilder(UriBuilder delegate, ParameterMarshaller marshaller) {
        this.delegate = delegate;
        this.marshaller = marshaller;
    }

    @Override
    public UriBuilder clone() {
        return new EncodedUriBuilder(delegate.clone(), marshaller);
    }

    @Override
    public UriBuilder uri(URI uri) {
        return new EncodedUriBuilder(delegate.uri(uri), marshaller);
    }

    @Override
    public UriBuilder uri(String uriTemplate) {
        return new EncodedUriBuilder(delegate.uri(uriTemplate), marshaller);
    }

    @Override
    public UriBuilder scheme(String scheme) {
        return new EncodedUriBuilder(delegate.scheme(scheme), marshaller);
    }

    @Override
    public UriBuilder schemeSpecificPart(String ssp) {
        return new EncodedUriBuilder(delegate.schemeSpecificPart(ssp), marshaller);
    }

    @Override
    public UriBuilder userInfo(String ui) {
        return new EncodedUriBuilder(delegate.userInfo(ui), marshaller);
    }

    @Override
    public UriBuilder host(String host) {
        return new EncodedUriBuilder(delegate.host(host), marshaller);
    }

    @Override
    public UriBuilder port(int port) {
        return new EncodedUriBuilder(delegate.port(port), marshaller);
    }

    @Override
    public UriBuilder replacePath(String path) {
        return new EncodedUriBuilder(delegate.replacePath(path), marshaller);
    }

    @Override
    public UriBuilder path(String path) {
        return new EncodedUriBuilder(delegate.path(path), marshaller);
    }

    @Override
    public UriBuilder path(Class resource) {
        return new EncodedUriBuilder(delegate.path(resource), marshaller);
    }

    @Override
    public UriBuilder path(Class resource, String method) {
        return new EncodedUriBuilder(delegate.path(resource, method), marshaller);
    }

    @Override
    public UriBuilder path(Method method) {
        return new EncodedUriBuilder(delegate.path(method), marshaller);
    }

    @Override
    public UriBuilder segment(String... segments) {
        return new EncodedUriBuilder(delegate.segment(segments), marshaller);
    }

    @Override
    public UriBuilder replaceMatrix(String matrix) {
        return new EncodedUriBuilder(delegate.replaceMatrix(matrix), marshaller);
    }

    @Override
    public UriBuilder matrixParam(String name, Object... values) {
        return new EncodedUriBuilder(delegate.matrixParam(name, marshalling(values)), marshaller);
    }

    @Override
    public UriBuilder replaceMatrixParam(String name, Object... values) {
        return new EncodedUriBuilder(delegate.replaceMatrixParam(name, marshalling(values)), marshaller);
    }

    @Override
    public UriBuilder replaceQuery(String query) {
        return new EncodedUriBuilder(delegate.replaceQuery(query), marshaller);
    }

    @Override
    public UriBuilder queryParam(String name, Object... values) {
        return new EncodedUriBuilder(delegate.queryParam(name, marshalling(values)), marshaller);
    }

    @Override
    public UriBuilder replaceQueryParam(String name, Object... values) {
        return new EncodedUriBuilder(delegate.replaceQueryParam(name, marshalling(values)), marshaller);
    }

    @Override
    public UriBuilder fragment(String fragment) {
        return new EncodedUriBuilder(delegate.fragment(fragment), marshaller);
    }

    @Override
    public UriBuilder resolveTemplate(String name, Object value) {
        return new EncodedUriBuilder(delegate.resolveTemplate(name, value), marshaller);
    }

    @Override
    public UriBuilder resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        return new EncodedUriBuilder(delegate.resolveTemplate(name, value, encodeSlashInPath), marshaller);
    }

    @Override
    public UriBuilder resolveTemplateFromEncoded(String name, Object value) {
        return new EncodedUriBuilder(delegate.resolveTemplateFromEncoded(name, value), marshaller);
    }

    @Override
    public UriBuilder resolveTemplates(Map<String, Object> templateValues) {
        return new EncodedUriBuilder(delegate.resolveTemplates(templateValues), marshaller);
    }

    @Override
    public UriBuilder resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath)
            throws IllegalArgumentException {
        return new EncodedUriBuilder(delegate.resolveTemplates(templateValues, encodeSlashInPath), marshaller);
    }

    @Override
    public UriBuilder resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        return new EncodedUriBuilder(delegate.resolveTemplatesFromEncoded(templateValues), marshaller);
    }

    @Override
    public URI buildFromMap(Map<String, ?> values) {
        return delegate.buildFromMap(values);
    }

    @Override
    public URI buildFromMap(Map<String, ?> values, boolean encodeSlashInPath)
            throws IllegalArgumentException, UriBuilderException {
        return delegate.buildFromMap(values, encodeSlashInPath);
    }

    @Override
    public URI buildFromEncodedMap(Map<String, ?> values) throws IllegalArgumentException, UriBuilderException {
        return delegate.buildFromEncodedMap(values);
    }

    @Override
    public URI build(Object... values) throws IllegalArgumentException, UriBuilderException {
        return delegate.build(values);
    }

    @Override
    public URI build(Object[] values, boolean encodeSlashInPath) throws IllegalArgumentException, UriBuilderException {
        return delegate.build(values, encodeSlashInPath);
    }

    @Override
    public URI buildFromEncoded(Object... values) throws IllegalArgumentException, UriBuilderException {
        return delegate.buildFromEncoded(values);
    }

    @Override
    public String toTemplate() {
        return delegate.toTemplate();
    }

    private Object[] marshalling(Object... objects) {
        if (objects == null) {
            return null;
        }
        return of(objects).map(marshaller::marshall).toArray();
    }
}
