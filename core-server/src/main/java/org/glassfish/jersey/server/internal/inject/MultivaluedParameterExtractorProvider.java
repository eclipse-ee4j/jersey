/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.internal.inject;

import org.glassfish.jersey.model.Parameter;

/**
 * Provider of multivalued parameter extractors.
 *
 * @author Paul Sandoz
 * @author Marek Potociar
 */
public interface MultivaluedParameterExtractorProvider {

    /**
     * Get the extractor configured to extract value of given {@link Parameter parameter}.
     * <p />
     * If the default value has been set on the parameter, it will be configured
     * in the extractor.
     *
     * @param parameter server model parameter.
     * @return extractor for the method parameter.
     */
    MultivaluedParameterExtractor<?> get(Parameter parameter);
}
