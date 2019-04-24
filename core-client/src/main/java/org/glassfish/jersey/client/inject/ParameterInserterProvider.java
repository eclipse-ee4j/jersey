/*
 * Copyright (c) 2010, 2017 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2018 Payara Foundation and/or its affiliates.
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

package org.glassfish.jersey.client.inject;

import org.glassfish.jersey.model.Parameter;

/**
 * Provider of parameter inserter.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 * @author Gaurav Gupta (gaurav.gupta@payara.fish)
 */
public interface ParameterInserterProvider {

    /**
     * Get the inserter configured to insert value of given {@link Parameter parameter}.
     * <p />
     * If the default value has been set on the parameter, it will be configured
     * in the inserter.
     *
     * @param parameter client model parameter.
     * @return inserter for the method parameter.
     */
    ParameterInserter<?, ?> get(Parameter parameter);
}
