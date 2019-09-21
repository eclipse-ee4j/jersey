/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.inject.Singleton;

import org.glassfish.jersey.message.filtering.spi.AbstractEntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessor;

@Singleton
@Priority(Integer.MAX_VALUE - 5000)
public class SelectableEntityProcessor extends AbstractEntityProcessor {

    protected Result process(final String fieldName, final Class<?> fieldClass, final Annotation[] fieldAnnotations,
                             final Annotation[] annotations, final EntityGraph graph) {

        if (fieldName != null) {
            final Set<String> scopes = new HashSet<>();

            // add default selectable scope in case of none requested
            scopes.add(SelectableScopeResolver.DEFAULT_SCOPE);

            // add specific scope in case of specific request
            scopes.add(SelectableScopeResolver.PREFIX + fieldName);

            addFilteringScopes(fieldName, fieldClass, scopes, graph);
        }

        return EntityProcessor.Result.APPLY;
    }

}
