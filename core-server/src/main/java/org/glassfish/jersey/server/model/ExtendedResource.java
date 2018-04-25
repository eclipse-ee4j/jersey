/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.model;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can mark resource classes or resource methods that should be considered as extended resources.
 * <p>
 * Extended resource model components are helper components that are not considered as a core of a
 * RESTful API. These can be for example {@code OPTIONS} {@link ResourceMethod resource methods}
 * added by {@link org.glassfish.jersey.server.model.ModelProcessor model processors}
 * or {@code application.wadl} resource producing the WADL. Both resource are rather supportive
 * than the core of RESTful API.
 * </p>
 * <p>
 * Marking resources and resource methods as extended can influence the way how these components
 * are processed in Jersey features like WADL generation or monitoring. Extended resources are
 * for example skipped when generation WADL in simple default format.
 * </p>
 *
 * @author Miroslav Fuksa
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExtendedResource {
}
