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

package org.glassfish.jersey.message.filtering;

import org.glassfish.jersey.message.filtering.spi.AbstractObjectProvider;
import org.glassfish.jersey.message.filtering.spi.ObjectGraph;

/**
 * {@link org.glassfish.jersey.message.filtering.spi.ObjectProvider Object provider} and
 * {@link org.glassfish.jersey.message.filtering.spi.ObjectGraphTransformer object graph transformer} implementation for
 * {@link ObjectGraph object graph}. These providers are present in runtime by default.
 *
 * @author Michal Gajdos
 */
final class ObjectGraphProvider extends AbstractObjectProvider<ObjectGraph> {

    @Override
    public ObjectGraph transform(final ObjectGraph graph) {
        return graph;
    }
}
