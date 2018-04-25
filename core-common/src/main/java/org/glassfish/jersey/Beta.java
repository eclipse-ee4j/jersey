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

package org.glassfish.jersey;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Marker of a public Jersey API that is still in "beta" non-final version.
 * <p>
 * This annotation signals that the annotated public Jersey API (package, class, method or field)
 * has not been fully stabilized yet. As such, the API is subject to backward-incompatible changes
 * (or even removal) in a future Jersey release. Jersey development team does not make any guarantees
 * to retain backward compatibility of a {@code @Beta}-annotated Jersey API.
 * </p>
 * <p>
 * This annotation does not indicate inferior quality or performance of the API, just informs that the
 * API may still evolve in the future in a backward-incompatible ways. Jersey users may use beta APIs
 * in their applications keeping in mind potential cost of extra work associated with an upgrade
 * to a newer Jersey version.
 * </p>
 * <p>
 * Once a {@code @Beta}-annotated Jersey API reaches the desired maturity, the {@code @Beta} annotation
 * will be removed from such API and the API will become part of a stable public Jersey API.
 * </p>
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
@Retention(RetentionPolicy.CLASS)
@Documented
@Target({ANNOTATION_TYPE, TYPE, CONSTRUCTOR, METHOD, FIELD, PACKAGE})
public @interface Beta {
}
