/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jaxb.internal;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.jaxb.FeatureSupplier;
import org.glassfish.jersey.jaxb.PropertySupplier;
import org.glassfish.jersey.model.internal.RankedComparator;

import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Utility class that sets features and properties
 */
final class JaxbFeatureUtil {

    private static final Logger LOGGER = Logger.getLogger(JaxbFeatureUtil.class.getName());
    private static final RankedComparator<PropertySupplier> PROPERTY_COMPARATOR
            = new RankedComparator<>(RankedComparator.Order.DESCENDING);
    private static final RankedComparator<FeatureSupplier> FEATURE_COMPARATOR
            = new RankedComparator<>(RankedComparator.Order.DESCENDING);

    private JaxbFeatureUtil() {
    }

    static void setFeatures(InjectionManager injectionManager, Class<?> clazz, Settable<Boolean> consumer) {
        if (injectionManager != null) {
            final Iterable<FeatureSupplier> featureSuppliers
                    = Providers.getAllProviders(injectionManager, FeatureSupplier.class, FEATURE_COMPARATOR);
            for (FeatureSupplier featureSupplier : featureSuppliers) {
                if (featureSupplier.isFor(clazz)) {
                    for (Map.Entry<String, Boolean> entry : featureSupplier.getFeatures().entrySet()) {
                        setFeature(clazz, entry, consumer);
                    }
                }
            }
        }
    }

    static void setProperties(InjectionManager injectionManager, Class<?> clazz, Settable<Object> consumer) {
        if (injectionManager != null) {
            final Iterable<PropertySupplier> propertySuppliers
                    = Providers.getAllProviders(injectionManager, PropertySupplier.class, PROPERTY_COMPARATOR);
            for (PropertySupplier propertySupplier : propertySuppliers) {
                if (propertySupplier.isFor(clazz)) {
                    for (Map.Entry<String, Object> entry : propertySupplier.getProperties().entrySet()) {
                        setProperty(clazz, entry, consumer);
                    }
                }
            }
        }
    }

    static <T> void setProperty(Class<?> clazz, Map.Entry<String, T> settable, Settable<T> consumer) {
        Optional<Exception> exception = consumer.accept(settable.getKey(), settable.getValue());
        exception.ifPresent((ex) -> LOGGER.warning(LocalizationMessages.CANNOT_SET_PROPERTY(
                settable.getKey(), settable.getValue(), clazz.getName(), ex)));
    }

    private static <T> void setFeature(Class<?> clazz, Map.Entry<String, T> settable, Settable<T> consumer) {
        Optional<Exception> exception = consumer.accept(settable.getKey(), settable.getValue());
        exception.ifPresent((ex) -> LOGGER.warning(LocalizationMessages.CANNOT_SET_FEATURE(
                settable.getKey(), settable.getValue(), clazz.getName(), ex)));
    }


    @FunctionalInterface
    static interface Settable<T> {
        void set(String key, T t) throws javax.xml.parsers.ParserConfigurationException,
                org.xml.sax.SAXNotRecognizedException, org.xml.sax.SAXNotSupportedException,
                javax.xml.transform.TransformerConfigurationException;

        default Optional<Exception> accept(String key, T t) {
            try {
                set(key, t);
                return Optional.empty();
            } catch (Exception e) {
                return Optional.of(e);
            }
        }
    }
}
