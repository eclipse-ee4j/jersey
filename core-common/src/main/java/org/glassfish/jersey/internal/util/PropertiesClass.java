/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for property classes.
 *
 * All static {@code String} fields in a class annotated by this annotation are considered to represent
 * property names recognized by Jersey runtime or one of the Jersey extension modules.
 * <p>
 * Putting this annotation on a class has the same effect as putting the {@link org.glassfish.jersey.internal.util.Property}
 * annotation on each individual static {@code String} field in the class.
 * </p>
 * <p>
 * Jersey code should not contain overlapping nor duplicate property names. This is checked in a dedicated
 * (@code org.glassfish.jersey.tests.integration.propertycheck.PropertyOverlappingCheckTest) unit test.
 * </p>
 *
 * @author Marek Potociar
 * @see org.glassfish.jersey.internal.util.Property
 * @see org.glassfish.jersey.internal.util.PropertyAlias
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertiesClass {
}
