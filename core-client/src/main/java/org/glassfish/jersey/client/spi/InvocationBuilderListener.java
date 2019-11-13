/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.client.spi;

import org.glassfish.jersey.Beta;
import org.glassfish.jersey.spi.Contract;

import javax.ws.rs.ConstrainedTo;
import javax.ws.rs.RuntimeType;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Locale;

/**
 * Implementations of this interface will be notified when a new Invocation.Builder
 * is created. This will allow implementations to access the invocation builders,
 * and is intended for global providers. For example, the Invocation.Builder properties can be
 * accessed to set properties that are available on the {@link javax.ws.rs.client.ClientRequestContext}.
 *
 * In order for the InvocationBuilderListener to be called, the implementation of the interface needs
 * to be registered on the {@code Client} the same way the {@code ClientRequestFilter} is registered, for instance.
 *
 * @since 2.30
 */
@Beta
@Contract
@ConstrainedTo(RuntimeType.CLIENT)
public interface InvocationBuilderListener {

    /**
     * An {@link javax.ws.rs.client.Invocation.Builder} subset of setter methods.
     */
    public interface InvocationBuilderContext {
        /**
         * Add the accepted response media types.
         *
         * @param mediaTypes accepted response media types.
         * @return the updated context.
         */
        InvocationBuilderContext accept(String... mediaTypes);

        /**
         * Add the accepted response media types.
         *
         * @param mediaTypes accepted response media types.
         * @return the updated context.
         */
        InvocationBuilderContext accept(MediaType... mediaTypes);

        /**
         * Add acceptable languages.
         *
         * @param locales an array of the acceptable languages.
         * @return the updated context.
         */
        InvocationBuilderContext acceptLanguage(Locale... locales);

        /**
         * Add acceptable languages.
         *
         * @param locales an array of the acceptable languages.
         * @return the updated context.
         */
        InvocationBuilderContext acceptLanguage(String... locales);

        /**
         * Add acceptable encodings.
         *
         * @param encodings an array of the acceptable encodings.
         * @return the updated context.
         */
        InvocationBuilderContext acceptEncoding(String... encodings);

        /**
         * Add a cookie to be set.
         *
         * @param cookie to be set.
         * @return the updated context.
         */
        InvocationBuilderContext cookie(Cookie cookie);

        /**
         * Add a cookie to be set.
         *
         * @param name  the name of the cookie.
         * @param value the value of the cookie.
         * @return the updated context.
         */
        InvocationBuilderContext cookie(String name, String value);

        /**
         * Set the cache control data of the message.
         *
         * @param cacheControl the cache control directives, if {@code null}
         *                     any existing cache control directives will be removed.
         * @return the updated context.
         */
        InvocationBuilderContext cacheControl(CacheControl cacheControl);

        /**
         * Add an arbitrary header.
         *
         * @param name  the name of the header
         * @param value the value of the header, the header will be serialized
         *              using a {@link javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate} if
         *              one is available via {@link javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(java.lang.Class)}
         *              for the class of {@code value} or using its {@code toString} method
         *              if a header delegate is not available. If {@code value} is {@code null}
         *              then all current headers of the same name will be removed.
         * @return the updated context.
         */
        InvocationBuilderContext header(String name, Object value);

        /**
         * Replaces all existing headers with the newly supplied headers.
         *
         * @param headers new headers to be set, if {@code null} all existing
         *                headers will be removed.
         * @return the updated context.
         */
        InvocationBuilderContext headers(MultivaluedMap<String, Object> headers);

        /**
         * Set a new property in the context of a request represented by this invocation builder.
         * <p>
         * The property is available for a later retrieval via {@link ClientRequestContext#getProperty(String)}
         * or {@link javax.ws.rs.ext.InterceptorContext#getProperty(String)}.
         * If a property with a given name is already set in the request context,
         * the existing value of the property will be updated.
         * Setting a {@code null} value into a property effectively removes the property
         * from the request property bag.
         * </p>
         *
         * @param name  property name.
         * @param value (new) property value. {@code null} value removes the property
         *              with the given name.
         * @return the updated context.
         * @see Invocation#property(String, Object)
         */
        InvocationBuilderContext property(String name, Object value);
    }

    /**
     * Whenever an {@link Invocation.Builder} is created, (i.e. when
     * {@link WebTarget#request()}, {@link WebTarget#request(String...)},
     * {@link WebTarget#request(MediaType...)} is called), this method would be invoked.
     * @param context the updated {@link InvocationBuilderContext}.
     */
    void onNewBuilder(InvocationBuilderContext context);

}
