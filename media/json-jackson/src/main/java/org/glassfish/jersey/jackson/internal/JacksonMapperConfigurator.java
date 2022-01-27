/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson.internal;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.LazyValue;
import org.glassfish.jersey.internal.util.collection.Value;
import org.glassfish.jersey.internal.util.collection.Values;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.Annotations;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JsonMapperConfigurator;

import java.security.AccessController;
import java.util.ArrayList;

public class JacksonMapperConfigurator extends JsonMapperConfigurator {
    public JacksonMapperConfigurator(ObjectMapper mapper, Annotations[] defAnnotations) {
        super(mapper, defAnnotations);
    }

    @Override
    protected AnnotationIntrospector _resolveIntrospectors(Annotations[] annotationsToUse) {
        // Let's ensure there are no dups there first, filter out nulls
        ArrayList<AnnotationIntrospector> intr = new ArrayList<AnnotationIntrospector>();
        for (Annotations a : annotationsToUse) {
            if (a != null) {
                _resolveIntrospector(a, intr);
            }
        }
        int count = intr.size();
        if (count == 0) {
            return AnnotationIntrospector.nopInstance();
        }
        AnnotationIntrospector curr = intr.get(0);
        for (int i = 1, len = intr.size(); i < len; ++i) {
            curr = AnnotationIntrospector.pair(curr, intr.get(i));
        }
        return curr;
    }

    protected void _resolveIntrospector(Annotations ann, ArrayList<AnnotationIntrospector> intr) {
        switch (ann) {
            case JAXB:
                /* For this, need to use indirection just so that error occurs
                 * when we get here, and not when this class is being loaded
                 */
                try {
                    if (_jaxbIntrospectorClass == null) {
                        _jaxbIntrospectorClass = JakartaXmlBindAnnotationIntrospector.class;
                    }
                    intr.add(JakartaXmlBindAnnotationIntrospector.class.newInstance());
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to instantiate JakartaXmlBindAnnotationIntrospector: "
                            + e.getMessage(), e);
                }

                if (jaxb2AnnotationIntrospector.get() == true) {
                    Class<? extends AnnotationIntrospector> tempJaxbIntrospectorClass = _jaxbIntrospectorClass;
                    _jaxbIntrospectorClass = null;
                    intr.add(super._resolveIntrospector(ann));
                    _jaxbIntrospectorClass = tempJaxbIntrospectorClass;
                }
                break;
            default:
                intr.add(super._resolveIntrospector(ann));
        }
    }

    private static LazyValue<Boolean> jaxb2AnnotationIntrospector = Values.lazy((Value<Boolean>) () -> {
        final Class<?> aClass = AccessController.doPrivileged(
                ReflectionHelper.classForNamePA("com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector")
        );
        return aClass != null;
    });
}
