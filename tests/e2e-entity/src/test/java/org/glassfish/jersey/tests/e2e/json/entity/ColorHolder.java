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

package org.glassfish.jersey.tests.e2e.json.entity;

import java.util.Formatter;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import org.glassfish.jersey.tests.e2e.json.JsonTestHelper;

/**
 * @author Michal Gajdos
 */
@SuppressWarnings("UnusedDeclaration")
@XmlRootElement
public class ColorHolder {

    private Set<Color> colors;

    public ColorHolder() {
    }

    public ColorHolder(final Set<Color> colors) {
        this.colors = colors;
    }

    public Set<Color> getColors() {
        return colors;
    }

    public void setColors(final Set<Color> colors) {
        this.colors = colors;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ColorHolder)) {
            return false;
        }

        final ColorHolder other = (ColorHolder) obj;

        return JsonTestHelper.areCollectionsEqual(this.colors, other.colors);
    }

    @Override
    public int hashCode() {
        return colors == null ? super.hashCode() : 19 * colors.hashCode();
    }

    @Override
    public String toString() {
        return new Formatter().format("CH(%s)", colors).toString();
    }
}
