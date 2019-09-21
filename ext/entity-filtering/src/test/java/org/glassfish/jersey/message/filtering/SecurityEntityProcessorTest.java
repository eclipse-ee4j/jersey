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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.glassfish.jersey.message.filtering.spi.EntityGraph;
import org.glassfish.jersey.message.filtering.spi.EntityProcessor;
import org.glassfish.jersey.message.filtering.spi.EntityProcessorContext;
import org.glassfish.jersey.message.filtering.spi.FilteringHelper;
import org.glassfish.jersey.message.filtering.spi.ScopeProvider;

import org.junit.Before;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * {@link org.glassfish.jersey.message.filtering.SecurityEntityProcessor} unit tests.
 *
 * @author Michal Gajdos
 */
@SuppressWarnings("JavaDoc")
public class SecurityEntityProcessorTest {

    private SecurityEntityProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new SecurityEntityProcessor();
    }

    @Test
    public void testProcessPermitAllClass() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(PermitAllEntity.class);

        final EntityGraph expected = new EntityGraphImpl(PermitAllEntity.class);
        expected.addFilteringScopes(FilteringHelper.getDefaultFilteringScope());

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessClass(PermitAllEntity.class, actual, forWriter);

            assertThat(result, equalTo(EntityProcessor.Result.APPLY));
            assertThat(actual, equalTo(expected));
        }
    }

    @Test
    public void testProcessDenyAllClass() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(DenyAllEntity.class);
        final EntityGraph expected = new EntityGraphImpl(DenyAllEntity.class);

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessClass(DenyAllEntity.class, actual, forWriter);

            assertThat(result, equalTo(EntityProcessor.Result.ROLLBACK));
            assertThat(actual, equalTo(expected));
        }
    }

    @Test
    public void testProcessRolesAllowedClass() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(RolesAllowedEntity.class);

        final EntityGraph expected = new EntityGraphImpl(RolesAllowedEntity.class);
        expected.addFilteringScopes(
                Arrays.asList(
                        SecurityHelper.getRolesAllowedScope("manager"), SecurityHelper.getRolesAllowedScope("client"))
                      .stream()
                      .collect(Collectors.toSet()));

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessClass(RolesAllowedEntity.class, actual, forWriter);

            assertThat(result, equalTo(EntityProcessor.Result.APPLY));
            assertThat(actual, equalTo(expected));
        }
    }

    private EntityProcessor.Result testProcessClass(final Class<?> clazz, final EntityGraph graph, final boolean forWriter)
            throws Exception {

        final EntityProcessorContext context = new EntityProcessorContextImpl(
                forWriter ? EntityProcessorContext.Type.CLASS_WRITER : EntityProcessorContext.Type.CLASS_READER,
                clazz, graph);

        return processor.process(context);
    }

    @Test
    public void testProcessPermitAllProperties() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(PermitAllEntity.class);

        final EntityGraph expected = new EntityGraphImpl(PermitAllEntity.class);
        expected.addField("field", ScopeProvider.DEFAULT_SCOPE);

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessProperty(PermitAllEntity.class, actual, forWriter);

            assertThat(result, equalTo(EntityProcessor.Result.APPLY));
            assertThat(actual, equalTo(expected));
        }
    }

    @Test
    public void testProcessDenyAllProperties() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(DenyAllEntity.class);
        final EntityGraph expected = new EntityGraphImpl(DenyAllEntity.class);

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessProperty(DenyAllEntity.class, actual, forWriter);

            assertThat(result, equalTo(EntityProcessor.Result.ROLLBACK));
            assertThat(actual, equalTo(expected));
        }
    }

    @Test
    public void testProcessRolesAllowedProperties() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(RolesAllowedEntity.class);
        final EntityGraph expected = new EntityGraphImpl(RolesAllowedEntity.class);

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessProperty(RolesAllowedEntity.class, actual, forWriter);

            if (forWriter) {
                expected.addField("field", SecurityHelper.getRolesAllowedScope("manager"));
            } else {
                expected.addField("field", SecurityHelper.getRolesAllowedScope("client"));
            }

            assertThat(result, equalTo(EntityProcessor.Result.APPLY));
            assertThat(actual, equalTo(expected));
        }
    }

    private EntityProcessor.Result testProcessProperty(final Class<?> clazz, final EntityGraph graph, final boolean forWriter)
            throws Exception {

        final Field field = clazz.getDeclaredField("field");
        final Method method = forWriter ? clazz.getMethod("getField") : clazz.getMethod("setField", String.class);

        final EntityProcessorContext context = new EntityProcessorContextImpl(
                forWriter ? EntityProcessorContext.Type.PROPERTY_WRITER : EntityProcessorContext.Type.PROPERTY_WRITER,
                field, method, graph);

        return processor.process(context);
    }

    @Test
    public void testProcessPermitAllAccessors() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(PermitAllEntity.class);
        actual.addFilteringScopes(FilteringHelper.getDefaultFilteringScope());

        final EntityGraph expected = new EntityGraphImpl(PermitAllEntity.class);
        expected.addFilteringScopes(FilteringHelper.getDefaultFilteringScope());
        expected.addSubgraph("subgraph", SubEntity.class, ScopeProvider.DEFAULT_SCOPE);

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessAccessor(PermitAllEntity.class, actual, forWriter);

            assertThat(result, equalTo(EntityProcessor.Result.APPLY));
            assertThat(actual, equalTo(expected));
        }
    }

    @Test
    public void testProcessDenyAllAccessors() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(DenyAllEntity.class);
        final EntityGraph expected = new EntityGraphImpl(DenyAllEntity.class);

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessAccessor(DenyAllEntity.class, actual, forWriter);

            assertThat(result, equalTo(EntityProcessor.Result.ROLLBACK));
            assertThat(actual, equalTo(expected));
        }
    }

    @Test
    public void testProcessRolesAllowedAccessor() throws Exception {
        final EntityGraph actual = new EntityGraphImpl(RolesAllowedEntity.class);
        final EntityGraph expected = new EntityGraphImpl(RolesAllowedEntity.class);

        for (final boolean forWriter : new boolean[] {true, false}) {
            final EntityProcessor.Result result = testProcessAccessor(RolesAllowedEntity.class, actual, forWriter);

            if (forWriter) {
                expected.addSubgraph("subgraph", SubEntity.class, SecurityHelper.getRolesAllowedScope("manager"));
            } else {
                expected.addSubgraph("subgraph", SubEntity.class, SecurityHelper.getRolesAllowedScope("client"));
            }

            assertThat(result, equalTo(EntityProcessor.Result.APPLY));
            assertThat(actual, equalTo(expected));
        }
    }

    private EntityProcessor.Result testProcessAccessor(final Class<?> clazz, final EntityGraph graph, final boolean forWriter)
            throws Exception {

        final Method method = forWriter ? clazz.getMethod("getSubgraph") : clazz.getMethod("setSubgraph", SubEntity.class);

        final EntityProcessorContext context = new EntityProcessorContextImpl(
                forWriter ? EntityProcessorContext.Type.PROPERTY_WRITER : EntityProcessorContext.Type.PROPERTY_WRITER,
                method, graph);

        return processor.process(context);
    }
}
