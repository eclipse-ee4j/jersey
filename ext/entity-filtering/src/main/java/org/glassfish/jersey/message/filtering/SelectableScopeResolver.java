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
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.glassfish.jersey.internal.util.Tokenizer;
import org.glassfish.jersey.message.filtering.spi.ScopeResolver;

@Singleton
public class SelectableScopeResolver implements ScopeResolver {

    /**
     * Prefix for all selectable scopes
     */
    public static final String PREFIX = SelectableScopeResolver.class.getName() + "_";

    /**
     * Scope used for selecting all fields, i.e.: when no filter is applied
     */
    public static final String DEFAULT_SCOPE = PREFIX + "*";

    /**
     * Query parameter name for selectable feature, set to default value
     */
    private static String SELECTABLE_PARAM_NAME = "select";

    @Context
    private Configuration configuration;

    @Context
    private UriInfo uriInfo;

    @PostConstruct
    private void init() {
        final String paramName = (String) configuration.getProperty(SelectableEntityFilteringFeature.QUERY_PARAM_NAME);
        SELECTABLE_PARAM_NAME = paramName != null ? paramName : SELECTABLE_PARAM_NAME;
    }

    @Override
    public Set<String> resolve(final Annotation[] annotations) {
        final Set<String> scopes = new HashSet<>();

        final List<String> fields = uriInfo.getQueryParameters().get(SELECTABLE_PARAM_NAME);
        if (fields != null && !fields.isEmpty()) {
            for (final String field : fields) {
                scopes.addAll(getScopesForField(field));
            }
        } else {
            scopes.add(DEFAULT_SCOPE);
        }
        return scopes;
    }

    private Set<String> getScopesForField(final String fieldName) {
        final Set<String> scopes = new HashSet<>();

        // add specific scope in case of specific request
        final String[] fields = Tokenizer.tokenize(fieldName, ",");
        for (final String field : fields) {
            final String[] subfields = Tokenizer.tokenize(field, ".");
            if (subfields.length == 0) {
                continue;
            }
            // in case of nested path, add first level as stand-alone to ensure subgraph is added
            scopes.add(SelectableScopeResolver.PREFIX + subfields[0]);
            if (subfields.length > 1) {
                scopes.add(SelectableScopeResolver.PREFIX + field);
            }
        }

        return scopes;
    }
}
