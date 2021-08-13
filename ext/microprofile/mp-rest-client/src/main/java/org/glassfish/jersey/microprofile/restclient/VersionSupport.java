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

package org.glassfish.jersey.microprofile.restclient;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.glassfish.jersey.microprofile.restclient.internal.LocalizationMessages;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;

import java.util.logging.Logger;

/**
 * Backward compatibility support not to throw an exception when an old API is used.
 */
abstract class VersionSupport {

    protected abstract RestClientBuilder _followRedirects(RestClientBuilder restClientBuilder, boolean follow);
    protected abstract RestClientBuilder _proxyAddress(RestClientBuilder restClientBuilder, String proxy);
    protected abstract RestClientBuilder _queryParamStyle(RestClientBuilder restClientBuilder, String style);

    private static final Logger logger = Logger.getLogger(VersionSupport.class.getName());

    // determine the version only once per jvm
    private static LazyValue<VersionSupport> currentVersion = Values.lazy((Value<VersionSupport>) () -> {
        final Class<?> restClientBuilderClass = RestClientBuilder.class;
        try {
            if (null != restClientBuilderClass.getMethod("followRedirects", boolean.class)) {
                return new Version20Support();
            }
        } catch (NoSuchMethodException e) {
            // VERSION 1.4
        }
        return new Version14Support();
    });

    static RestClientBuilder followRedirects(RestClientBuilder restClientBuilder, boolean follow) {
        return currentVersion.get()._followRedirects(restClientBuilder, follow);
    }

    static RestClientBuilder proxyAddress(RestClientBuilder restClientBuilder, String proxy) {
        return currentVersion.get()._proxyAddress(restClientBuilder, proxy);
    }

    static RestClientBuilder queryParamStyle(RestClientBuilder restClientBuilder, String style) {
        return currentVersion.get()._queryParamStyle(restClientBuilder, style);
    }

    private static class Version14Support extends VersionSupport {
        protected RestClientBuilder _followRedirects(RestClientBuilder restClientBuilder, boolean follow) {
            logger.warning(LocalizationMessages.WARN_VERSION_14_FOLLOWREDIRECT());
            return restClientBuilder;
        }

        protected RestClientBuilder _proxyAddress(RestClientBuilder restClientBuilder, String proxy) {
            logger.warning(LocalizationMessages.WARN_VERSION_14_PROXY());
            return restClientBuilder;
        }

        protected RestClientBuilder _queryParamStyle(RestClientBuilder restClientBuilder, String style) {
            logger.warning(LocalizationMessages.WARN_VERSION_14_QUERYPARAMSTYLE());
            return restClientBuilder;
        }
    }

    private static class Version20Support extends VersionSupport {
        protected RestClientBuilder _followRedirects(RestClientBuilder restClientBuilder, boolean follow) {
            return restClientBuilder.followRedirects(follow);
        }

        protected RestClientBuilder _proxyAddress(RestClientBuilder restClientBuilder, String proxy) {
            int index = proxy.lastIndexOf(':');
            //If : was not found at all or it is the last character of the proxy string
            if (index < 0 || proxy.length() - 1 == index) {
                throw new IllegalArgumentException(LocalizationMessages.ERR_INVALID_PROXY_URI(proxy));
            }
            String proxyHost = proxy.substring(0, index);
            int proxyPort;
            String proxyPortStr = proxy.substring(index + 1);
            try {
                proxyPort = Integer.parseInt(proxyPortStr);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(LocalizationMessages.ERR_INVALID_PROXY_PORT(proxyPortStr), nfe);
            }
            return restClientBuilder.proxyAddress(proxyHost, proxyPort);
        }

        protected RestClientBuilder _queryParamStyle(RestClientBuilder restClientBuilder, String style) {
            // do not import for compatibility with 1.4
            org.eclipse.microprofile.rest.client.ext.QueryParamStyle queryParamStyle =
                    org.eclipse.microprofile.rest.client.ext.QueryParamStyle.valueOf(style);
            return restClientBuilder.queryParamStyle(queryParamStyle);
        }
    }
}
