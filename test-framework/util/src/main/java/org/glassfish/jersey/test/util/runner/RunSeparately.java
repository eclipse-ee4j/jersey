/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.test.util.runner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for test methods that should be run separately in a non-parallel
 * fashion. This is only taken into account when the test class is being processed
 * by any of Jersey provided parallel test runner {@link ConcurrentRunner}.
 *
 * @author Jakub Podlesak
 *
 * @deprecated in connection with transition to JUnit 5 usage of this class is obsolete. Alternatively can be used
 * specific junit 5
 * <a href="https://junit.org/junit5/docs/current/user-guide/#writing-tests-parallel-execution">executions tools</a>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Deprecated
public @interface RunSeparately {
}
