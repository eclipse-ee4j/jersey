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

package org.glassfish.jersey.media.multipart;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Custom {@link java.util.List} implementation that maintains parentage information automatically.
 *
 * @author Michal Gajdos
 */
class BodyPartsList extends ArrayList<BodyPart> {

    MultiPart parent = null;

    BodyPartsList(MultiPart parent) {
        this.parent = parent;
    }

    @Override
    public boolean add(BodyPart bp) {
        super.add(bp);
        bp.setParent(parent);
        return true;
    }

    @Override
    public void add(int index, BodyPart bp) {
        super.add(index, bp);
        bp.setParent(parent);
    }

    @Override
    public boolean addAll(Collection<? extends BodyPart> bps) {
        if (super.addAll(bps)) {
            for (BodyPart bp : bps) {
                bp.setParent(parent);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addAll(int index, Collection<? extends BodyPart> bps) {
        if (super.addAll(index, bps)) {
            for (BodyPart bp : bps) {
                bp.setParent(parent);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void clear() {
        BodyPart bps[] = super.toArray(new BodyPart[super.size()]);
        super.clear();
        for (BodyPart bp : bps) {
            bp.setParent(null);
        }
    }

    @Override
    public BodyPart remove(int index) {
        BodyPart bp = super.remove(index);
        if (bp != null) {
            bp.setParent(null);
        }
        return bp;
    }

    @Override
    public boolean remove(Object bp) {
        if (super.remove(bp)) {
            ((BodyPart) bp).setParent(null);
            return true;
        } else {
            return false;
        }
    }

}
