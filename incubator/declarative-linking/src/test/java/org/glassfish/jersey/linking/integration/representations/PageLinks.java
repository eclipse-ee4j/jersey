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

package org.glassfish.jersey.linking.integration.representations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.jersey.linking.Binding;
import org.glassfish.jersey.linking.ProvideLink;

@ProvideLink(value = ProvideLink.InheritFromAnnotation.class, rel = "next", bindings = {
        @Binding(name = "page", value = "${instance.number + 1}"),
        @Binding(name = "size", value = "${instance.size}"),
}, condition = "${instance.nextPageAvailable}")
@ProvideLink(value = ProvideLink.InheritFromAnnotation.class, rel = "prev", bindings = {
        @Binding(name = "page", value = "${instance.number - 1}"),
        @Binding(name = "size", value = "${instance.size}"),
}, condition = "${instance.previousPageAvailable}")
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PageLinks {
    Class<?> value();
}
