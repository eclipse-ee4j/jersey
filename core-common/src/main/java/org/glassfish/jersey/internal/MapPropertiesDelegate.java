/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Properties delegate backed by a {@code Map}.
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class MapPropertiesDelegate implements PropertiesDelegate {

    private final Map<String, Object> store;

    /**
     * Create new map-based properties delegate.
     */
    public MapPropertiesDelegate() {
        this.store = new HashMap<String, Object>();
    }

    /**
     * Create new map-based properties delegate.
     *
     * @param store backing property store.
     */
    public MapPropertiesDelegate(Map<String, Object> store) {
        this.store = store;
    }

    /**
     * Initialize new map-based properties delegate from another
     * delegate.
     *
     * @param that original properties delegate.
     */
    public MapPropertiesDelegate(PropertiesDelegate that) {
        if (that instanceof MapPropertiesDelegate) {
            this.store = new HashMap<String, Object>(((MapPropertiesDelegate) that).store);
        } else {
            this.store = new HashMap<String, Object>();
            for (String name : that.getPropertyNames()) {
                this.store.put(name, that.getProperty(name));
            }
        }
    }

    @Override
    public Object getProperty(String name) {
        return store.get(name);
    }

    @Override
    public Collection<String> getPropertyNames() {
        return Collections.unmodifiableCollection(store.keySet());
    }

    @Override
    public void setProperty(String name, Object value) {
        store.put(name, value);
    }

    @Override
    public void removeProperty(String name) {
        store.remove(name);
    }
}
