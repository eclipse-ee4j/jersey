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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.glassfish.jersey.Beta;

/**
 * Specifies the binding between a URI template parameter and a bean property.
 * @see org.glassfish.jersey.linking.InjectLink#bindings()
 *
 * @author Mark Hadley
 * @author Gerard Davison (gerard.davison at oracle.com)
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
@Beta
public @interface Binding {

    /**
     * Specifies the name of the URI template parameter, defaults to
     * "value" for convenience.
     */
    String name() default "value";

    /**
     * Specifies the value of a URI template parameter. The value is an EL
     * expression using immediate evaluation syntax. E.g.:
     * <pre>${instance.widgetId}</pre>
     * In the above example the value is taken from the <code>widgetId</code>
     * property of the implicit <code>instance</code> bean.
     * <p>Three implicit beans are supported:</p>
     * <dl>
     * <dt><code>instance</code></dt><dd>The object whose class contains the
     * {@link org.glassfish.jersey.linking.InjectLink} annotation.</dd>
     * <dt><code>entity</code></dt><dd>The entity returned by the resource
     * class method. This is either the resource method return value
     * or the entity property for a resource method that returns Response.</dd>
     * <dt><code>resource</code></dt><dd>The resource class instance that
     * returned the object that contains the {@code InjectLink} annotation.</dd>
     * </dd>
     * </dl>
     */
    String value();
}
