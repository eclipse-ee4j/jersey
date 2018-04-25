/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;

import javax.ws.rs.core.Link;

import org.glassfish.jersey.Beta;

/**
 * Use this on resource methods to contribute links to a representation.
 *
 * It is the inverse of {@link InjectLink} instead of annotating the target you annotate the source of the links.
 * The added benefit is that since you annotate the method you don't need to specify the path to it.
 *
 * <p>
 * <pre>
 * &#64;ProvideLink(value = Order.class, rel = "self", bindings = @Binding(name = "orderId", value = "${instance.id}"))
 * &#64;ProvideLink(value = PaymentConfirmation.class, rel = "order",
 *                  bindings = @Binding(name = "orderId", value = "${instance.orderId}"))
 * public Response get(@PathParam("orderId") String orderId) { ...
 * </pre>
 * </p>
 *
 * It can also be used as a meta annotation, see the Javadoc of {@link InheritFromAnnotation} for details.
 *
 * @author Leonard Brünings
 */
@Target({ ElementType.METHOD, ElementType.TYPE})
@Repeatable(ProvideLinks.class)
@Retention(RetentionPolicy.RUNTIME)
@Beta
public @interface ProvideLink {

    /**
     * The style of URI to inject
     */
    InjectLink.Style style() default InjectLink.Style.ABSOLUTE_PATH;

    /**
     * Provide links for representation classes listed here.
     *
     * May use {@link InheritFromAnnotation} for Meta-Annotations
     */
    Class<?>[] value();

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

    //
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
    InjectLink.Extension[] extensions() default {};


    class Util {

        static Link buildLinkFromUri(URI uri, ProvideLink link) {

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

    /**
     * Special interface to indicate that the target should be inherited from the annotated annotation.
     * <p>
     * <pre>
     * &#64;ProvideLinks({
     *   &#64;ProvideLink(value = ProvideLink.InheritFromAnnotation.class, rel = "next", bindings = {
     *       &#64;Binding(name = "page", value = "${instance.number + 1}"),
     *       &#64;Binding(name =&#64; "size", value = "${instance.size}"),
     *     },
     *     condition = "${instance.nextPageAvailable}"),
     *   &#64;ProvideLink(value = ProvideLink.InheritFromAnnotation.class, rel = "prev", bindings = {
     *       &#64;Binding(name = "page", value = "${instance.number - 1}"),
     *       &#64;Binding(name = "size", value = "${instance.size}"),
     *     },
     *     condition = "${instance.previousPageAvailable}")
     * })
     * &#64;Target({ElementType.METHOD})
     * &#64;Retention(RetentionPolicy.RUNTIME)
     * &#64;Documented
     * public &#64;interface PageLinks {
     * Class<?> value();
     * }
     * </pre>
     * </p>
     * <p>
     * In this case the value of each {@link ProvideLink} will be the same as {@code PageLinks} value.
     * </p>
     */
    interface InheritFromAnnotation{}
}
