package org.glassfish.jersey.microprofile.restclient;

import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

public class RestClientUriBuilder extends JerseyUriBuilder {

    private QueryParamStyle queryParamStyle = null;

    public RestClientUriBuilder() {
    }

    protected RestClientUriBuilder(final RestClientUriBuilder that) {
        super(that);
    }

    /**
     * Create a new instance initialized from an existing URI.
     *
     * @param uri a URI that will be used to initialize the UriBuilder.
     * @return a new UriBuilder.
     * @throws IllegalArgumentException if uri is {@code null}.
     */
    public static UriBuilder fromUri(URI uri) {
        return new RestClientUriBuilder().uri(uri);
    }

    public QueryParamStyle getQueryParamStyle() {
        return queryParamStyle;
    }

    public void setQueryParamStyle(QueryParamStyle queryParamStyle) {
        this.queryParamStyle = queryParamStyle;
    }

    @Override
    public JerseyUriBuilder queryParam(String name, final Object... values) {
        checkSsp();
        if (name == null) {
            throw new IllegalArgumentException(LocalizationMessages.PARAM_NULL("name"));
        }
        if (values == null) {
            throw new IllegalArgumentException(LocalizationMessages.PARAM_NULL("values"));
        }
        if (values.length == 0) {
            return this;
        }
        name = encode(name, UriComponent.Type.QUERY_PARAM);

        if (queryParamStyle == QueryParamStyle.ARRAY_PAIRS) {
            clientQueryParamArrayPairs(name, values);
        } else if (queryParamStyle == QueryParamStyle.COMMA_SEPARATED) {
            clientQueryParamCommaSeparated(name, values);
        } else {
            clientQueryParamMultiPairs(name, values);
        }
        return this;
    }

    /**
     * Multiple parameter instances, e.g foo=v1&amp;foot=v2&amp;foo=v3 This is
     * the default if no style is configured.
     *
     * @param name
     * @param values
     * @throws IllegalArgumentException
     */
    private void clientQueryParamMultiPairs(String name, final Object... values) {
        if (queryParams == null) {
            for (final Object value : values) {
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(name);

                if (value == null) {
                    throw new IllegalArgumentException(LocalizationMessages.QUERY_PARAM_NULL());
                }

                query.append('=').append(encode(value.toString(), UriComponent.Type.QUERY_PARAM));
            }
        } else {
            for (final Object value : values) {
                if (value == null) {
                    throw new IllegalArgumentException(LocalizationMessages.QUERY_PARAM_NULL());
                }

                queryParams.add(name, encode(value.toString(), UriComponent.Type.QUERY_PARAM));
            }
        }
    }

    /**
     * A single parameter instance with multiple, comma-separated values, e.g
     * key=value1,value2,value3.
     *
     * @param name
     * @param values
     * @throws IllegalArgumentException
     */
    private void clientQueryParamCommaSeparated(String name, final Object... values) throws IllegalArgumentException {
        StringBuilder sb = new StringBuilder();
        if (queryParams == null) {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(name);
            int valuesCount = values.length - 1;
            for (final Object value : values) {
                if (value == null) {
                    throw new IllegalArgumentException(LocalizationMessages.QUERY_PARAM_NULL());
                }
                sb.append(encode(value.toString(), UriComponent.Type.QUERY_PARAM));
                if (valuesCount > 0) {
                    sb.append(",");
                    --valuesCount;
                }
            }
            query.append('=').append(sb.toString());
        } else {
            int valuesCount = values.length - 1;
            for (final Object value : values) {
                if (value == null) {
                    throw new IllegalArgumentException(LocalizationMessages.QUERY_PARAM_NULL());
                }
                sb.append(encode(value.toString(), UriComponent.Type.QUERY_PARAM));
                if (valuesCount > 0) {
                    sb.append(",");
                    --valuesCount;
                }
            }
            queryParams.add(name, sb.toString());
        }

    }

    /**
     * Multiple parameter instances with square brackets for each parameter, e.g
     * key[]=value1&key[]=value2&key[]=value3.
     *
     * @param name
     * @param values
     * @throws IllegalArgumentException
     */
    private void clientQueryParamArrayPairs(String name, final Object... values) throws IllegalArgumentException {
        if (queryParams == null) {
            for (final Object value : values) {
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(name).append("[]");

                if (value == null) {
                    throw new IllegalArgumentException(LocalizationMessages.QUERY_PARAM_NULL());
                }

                query.append('=').append(encode(value.toString(), UriComponent.Type.QUERY_PARAM));
            }
        } else {
            for (final Object value : values) {
                if (value == null) {
                    throw new IllegalArgumentException(LocalizationMessages.QUERY_PARAM_NULL());
                }

                queryParams.add(name + "[]", encode(value.toString(), UriComponent.Type.QUERY_PARAM));
            }
        }
    }

    @Override
    public RestClientUriBuilder clone() {
        return new RestClientUriBuilder(this);
    }
}
