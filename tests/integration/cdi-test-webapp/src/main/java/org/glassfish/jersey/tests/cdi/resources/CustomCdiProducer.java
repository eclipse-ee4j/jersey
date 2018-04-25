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

package org.glassfish.jersey.tests.cdi.resources;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.inject.Produces;

/**
 * CDI producer to help us make sure HK2 do not mess up with
 * types backed by CDI producers.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class CustomCdiProducer {

    /**
     * Custom qualifier to work around https://java.net/jira/browse/GLASSFISH-20285
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
    @javax.inject.Qualifier
    public static @interface Qualifier {
    }

    /**
     * To cover field producer.
     */
    @Produces
    public static FieldProducedBean<String> field = new FieldProducedBean<>("field");

    /**
     * To cover method producer.
     *
     * @return bean instance to inject
     */
    @Produces
    public MethodProducedBean<String> produceBean() {
        return new MethodProducedBean<>("method");
    }

    /**
     * Part of JERSEY-2526 reproducer. This one is used
     * to inject constructor of {@link ConstructorInjectedResource}.
     *
     * @return fixed string value.
     */
    @Produces
    @Qualifier
    public String produceString() {
        return "cdi-produced";
    }
}
