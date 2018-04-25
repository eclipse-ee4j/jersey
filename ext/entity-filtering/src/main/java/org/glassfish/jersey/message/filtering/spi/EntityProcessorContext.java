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

package org.glassfish.jersey.message.filtering.spi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * {@link EntityProcessor Entity processor} context providing details about entity processing.
 * <p/>
 * The context contains the {@link Type} which distinguishes between types of context. There are various properties in the
 * context (accessible by getters) and some of them might be relevant only to specific context types.
 *
 * @author Michal Gajdos
 */
public interface EntityProcessorContext {

    /**
     * The type of the context which describes in which entity processing phase the
     * {@link EntityProcessor#process(EntityProcessorContext)} is triggered.
     */
    public enum Type {

        /**
         * Context created to process entity class when reading entity from an input stream into an Java object. Properties
         * available for this type are: {@link #getEntityClass()}, {@link #getEntityGraph()}.
         */
        CLASS_READER,

        /**
         * Context created to process entity class when writing entity to an output stream from an Java object. Properties
         * available for this type are: {@link #getEntityClass()}, {@link #getEntityGraph()}.
         */
        CLASS_WRITER,

        /**
         * Context created to process entity properties when reading entity from an input stream into an Java object. Properties
         * available for this type are: {@link #getField()}, {@link #getMethod()}, {@link #getEntityGraph()}.
         */
        PROPERTY_READER,

        /**
         * Context created to process entity properties when writing entity to an output stream from an Java object. Properties
         * available for this type are: {@link #getField()}, {@link #getMethod()}, {@link #getEntityGraph()}.
         */
        PROPERTY_WRITER,

        /**
         * Context created to process property accessors when reading entity from an input stream into an Java object. Properties
         * available for this type are: {@link #getMethod()}, {@link #getEntityGraph()}.
         */
        METHOD_READER,

        /**
         * Context created to process property accessors when writing entity to an output stream from an Java object. Properties
         * available for this type are: {@link #getMethod()}, {@link #getEntityGraph()}.
         */
        METHOD_WRITER
    }

    /**
     * Get the {@link Type type} of this context.
     *
     * @return entity processing context type.
     */
    public Type getType();

    /**
     * Get entity class to be processed. The entity class is available only for {@link Type#CLASS_WRITER} and
     * {@link Type#CLASS_READER} context types.
     *
     * @return entity class or {@code null} if the class is not available.
     */
    public Class<?> getEntityClass();

    /**
     * Get field to be processed. The field is available only for {@link Type#PROPERTY_WRITER} and
     * {@link Type#PROPERTY_READER} context types.
     *
     * @return field or {@code null} if the field is not available.
     */
    public Field getField();

    /**
     * Get method to be processed. The method is available for {@link Type#PROPERTY_WRITER}, {@link Type#PROPERTY_READER},
     * {@link Type#METHOD_WRITER}, {@link Type#METHOD_READER} context types.
     *
     * @return method or {@code null} if the method is not available.
     */
    public Method getMethod();

    /**
     * Get entity graph to be modified by the processing. The entity graph is available for all context types.
     *
     * @return entity graph.
     */
    public EntityGraph getEntityGraph();
}
