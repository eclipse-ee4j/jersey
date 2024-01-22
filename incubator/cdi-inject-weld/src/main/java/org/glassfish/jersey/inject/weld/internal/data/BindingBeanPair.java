/*
 * Copyright (c) 2021, 2024 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.jersey.inject.weld.internal.data;

import org.glassfish.jersey.inject.weld.internal.bean.JerseyBean;
import org.glassfish.jersey.internal.inject.Binding;

import java.util.ArrayList;
import java.util.List;

/**
 * Pair of a binding and corresponding Jersey beans.
 */
public class BindingBeanPair {
    private final Binding binding;
    private final List<JerseyBean> beans = new ArrayList<JerseyBean>();

    public BindingBeanPair(Binding binding, JerseyBean... beans) {
        this.binding = binding;
        if (beans != null) {
            for (JerseyBean bean : beans) {
                this.beans.add(bean);
            }
        }
    }

    public Binding getBinding() {
        return binding;
    }

    public List<JerseyBean> getBeans() {
        return beans;
    }
}
