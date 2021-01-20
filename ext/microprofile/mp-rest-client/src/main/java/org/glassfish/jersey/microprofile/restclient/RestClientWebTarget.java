package org.glassfish.jersey.microprofile.restclient;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.glassfish.jersey.internal.guava.Preconditions;

public class RestClientWebTarget extends JerseyWebTarget {

    /**
     * Create new web target instance.
     *
     * @param uri target URI.
     * @param parent parent client.
     */
    /*package*/ RestClientWebTarget(String uri, JerseyClient parent) {
        super(RestClientUriBuilder.fromUri(uri), parent.getConfiguration());
    }

    /**
     * Create new web target instance.
     *
     * @param uri target URI.
     * @param parent parent client.
     */
    /*package*/ RestClientWebTarget(URI uri, JerseyClient parent) {
        super(RestClientUriBuilder.fromUri(uri), parent.getConfiguration());
    }

    /**
     * Create new web target instance.
     *
     * @param uriBuilder builder for the target URI.
     * @param parent parent client.
     */
    /*package*/ RestClientWebTarget(UriBuilder uriBuilder, JerseyClient parent) {
        super(uriBuilder.clone(), parent.getConfiguration());
    }

    /**
     * Create new web target instance.
     *
     * @param link link to the target URI.
     * @param parent parent client.
     */
    /*package*/ RestClientWebTarget(Link link, JerseyClient parent) {
        super(RestClientUriBuilder.fromUri(link.getUri()), parent.getConfiguration());
    }

    /**
     * Create new web target instance.
     *
     * @param uriBuilder builder for the target URI.
     * @param that original target to copy the internal data from.
     */
    protected RestClientWebTarget(UriBuilder uriBuilder, JerseyWebTarget that) {
        super(uriBuilder, that.getConfiguration());
    }

    @Override
    public JerseyWebTarget path(String path) throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(path, "path is 'null'.");

        return new RestClientWebTarget(getUriBuilder().path(path), this);
    }

    @Override
    public JerseyWebTarget matrixParam(String name, Object... values) throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(name, "Matrix parameter name must not be 'null'.");

        if (values == null || values.length == 0 || (values.length == 1 && values[0] == null)) {
            return new RestClientWebTarget(getUriBuilder().replaceMatrixParam(name, (Object[]) null), this);
        }

        checkForNullValues(name, values);
        return new RestClientWebTarget(getUriBuilder().matrixParam(name, values), this);
    }

    @Override
    public JerseyWebTarget queryParam(String name, Object... values) throws NullPointerException {
        checkNotClosed();
        UriBuilder uriBuilder = getUriBuilder();
        if (uriBuilder instanceof RestClientUriBuilder
                && ((RestClientUriBuilder) uriBuilder).getQueryParamStyle() == null) {
            QueryParamStyle paramStyle = (QueryParamStyle) this.getConfiguration()
                    .getProperty(QueryParamStyle.class.getSimpleName());
            ((RestClientUriBuilder) uriBuilder).setQueryParamStyle(paramStyle);
        }
        return new RestClientWebTarget(RestClientWebTarget.setQueryParam(uriBuilder, name, values), this);
    }

    @Override
    public JerseyWebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(name, "name is 'null'.");
        Preconditions.checkNotNull(value, "value is 'null'.");

        return new RestClientWebTarget(getUriBuilder().resolveTemplate(name, value, encodeSlashInPath), this);
    }

    @Override
    public JerseyWebTarget resolveTemplateFromEncoded(String name, Object value)
            throws NullPointerException {
        checkNotClosed();
        Preconditions.checkNotNull(name, "name is 'null'.");
        Preconditions.checkNotNull(value, "value is 'null'.");

        return new RestClientWebTarget(getUriBuilder().resolveTemplateFromEncoded(name, value), this);
    }

    @Override
    public JerseyWebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath)
            throws NullPointerException {
        checkNotClosed();
        checkTemplateValues(templateValues);

        if (templateValues.isEmpty()) {
            return this;
        } else {
            return new RestClientWebTarget(getUriBuilder().resolveTemplates(templateValues, encodeSlashInPath), this);
        }
    }

    @Override
    public JerseyWebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues)
            throws NullPointerException {
        checkNotClosed();
        checkTemplateValues(templateValues);

        if (templateValues.isEmpty()) {
            return this;
        } else {
            return new RestClientWebTarget(getUriBuilder().resolveTemplatesFromEncoded(templateValues), this);
        }
    }

}
