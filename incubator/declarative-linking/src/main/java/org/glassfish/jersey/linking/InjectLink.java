/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.linking;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.Beta;

/**
 * Specifies a link injection target in a returned representation bean. May be
 * used on fields of type String or URI. One of {@link #value()} or
 * {@link #resource()} must be specified.
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Beta
public @interface InjectLink {

    /**
     * Styles of URI supported
     */
    enum Style {

        /**
         * An absolute URI. The URI template will be prefixed with the absolute
         * base URI of the application.
         */
        ABSOLUTE,
        /**
         * An absolute path. The URI template will be prefixed with the absolute
         * base path of the application.
         */
        ABSOLUTE_PATH,
        /**
         * A relative path. The URI template will be converted to a relative
         * path with no prefix.
         */
        RELATIVE_PATH

    }

    /**
     * The style of URI to inject
     */
    Style style() default Style.ABSOLUTE_PATH;

    /**
     * Specifies a URI template that will be used to build the injected URI. The
     * template may contain both URI template parameters (e.g. {id}) and EL
     * expressions (e.g. ${instance.id}) using the same implicit beans as
     * {@link Binding#value()}. URI template parameter values are resolved as
     * described in {@link #resource()}. E.g. the following three alternatives
     * are equivalent:
     * <pre>
     * &#64;Ref("{id}")
     * &#64;Ref(value="{id}", bindings={
     *   &#64;Binding(name="id" value="${instance.id}"}
     * )
     * &#64;Ref("${instance.id}")
     * </pre>
     */
    String value() default "";

    /**
     * Specifies a resource class whose @Path URI template will be used to build
     * the injected URI. Embedded URI template parameter values are resolved as
     * follows:
     * <ol>
     * <li>If the {@link #bindings()} property contains a binding specification
     * for the parameter then that is used</li>
     * <li>Otherwise an implicit binding is used that extracts the value of a
     * bean property by the same name as the URI template from the implicit
     * {@code instance} bean (see {@link Binding}).</li>
     * </ol>
     * <p>
     * E.g. assuming a resource class {@code SomeResource} with the
     * following {@code @Path("{id}")} annotation, the following two
     * alternatives are therefore equivalent:</p>
     * <pre>
     * &#64;Ref(resource=SomeResource.class)
     * &#64;Ref(resource=SomeResource.class, bindings={
     *   &#64;Binding(name="id" value="${instance.id}"}
     * )
     * </pre>
     */
    Class<?> resource() default Class.class;

    /**
     * Used in conjunction with {@link #resource()} to specify a subresource
     * locator or method. The value is the name of the method. The value of the
     * method's @Path annotation will be appended to the value of the
     * class-level @Path annotation separated by '/' if necessary.
     */
    String method() default "";

    /**
     * Specifies the bindings for embedded URI template parameters.
     *
     * @see Binding
     */
    Binding[] bindings() default {};

    /**
     * Specifies a boolean EL expression whose value determines whether a Ref is
     * set (true) or not (false). Omission of a condition will always insert a
     * ref.
     */
    String condition() default "";

    // Link properties
    //

    /**
     * Specifies the relationship.
     */
    String rel() default "";

    /**
     * Specifies the reverse relationship.
     */
    String rev() default "";

    /**
     * Specifies the media type.
     */
    String type() default "";

    /**
     * Specifies the title.
     */
    String title() default "";

    /**
     * Specifies the anchor
     */
    String anchor() default "";

    /**
     * Specifies the media
     */
    String media() default "";

    /**
     * Specifies the lang of the referenced resource
     */
    String hreflang() default "";

    /**
     * Specifies extension parameters as name-value pairs.
     */
    Extension[] extensions() default {};

    @Target({ElementType.TYPE, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Extension {

        /**
         * Specifies the name of the extension parameter
         */
        String name();

        /**
         * Specifies the value of the extension parameter
         */
        String value();
    }

    class Util {

        public static Link buildLinkFromUri(URI uri, InjectLink link) {

            javax.ws.rs.core.Link.Builder builder = javax.ws.rs.core.Link.fromUri(uri);
            if (!link.rel().isEmpty()) {
                builder = builder.rel(link.rel());
            }
            if (!link.rev().isEmpty()) {
                builder = builder.param("rev", link.rev());
            }
            if (!link.type().isEmpty()) {
                builder = builder.type(link.type());
            }
            if (!link.title().isEmpty()) {
                builder = builder.param("title", link.title());
            }
            if (!link.anchor().isEmpty()) {
                builder = builder.param("anchor", link.anchor());
            }
            if (!link.media().isEmpty()) {
                builder = builder.param("media", link.media());
            }
            if (!link.hreflang().isEmpty()) {
                builder = builder.param("hreflang", link.hreflang());
            }
            for (InjectLink.Extension ext : link.extensions()) {
                builder = builder.param(ext.name(), ext.value());
            }
            return builder.build();
        }
    }

}
