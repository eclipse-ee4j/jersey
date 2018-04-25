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

package org.glassfish.jersey.internal.inject;

import javax.ws.rs.core.Context;

/**
 * A marker interface to {@code InjectionResolver&lt;Context&gt;}. This interface must be implemented by every Dependency
 * Injection Provider to properly handle the injection of {@link Context} annotation.
 * <p>
 * Jersey cannot simply add the default implementation of this interface because the proper implementation requires a lot of
 * caching and optimization which can be done only with very close dependency to DI provider.
 */
public interface ContextInjectionResolver extends InjectionResolver<Context> {
}
