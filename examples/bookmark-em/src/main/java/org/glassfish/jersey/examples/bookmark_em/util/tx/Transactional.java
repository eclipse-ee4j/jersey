/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.bookmark_em.util.tx;

import javax.persistence.EntityManager;

/**
 * @author Paul Sandoz
 */
public abstract class Transactional {

    EntityManager em;
    boolean joinTransaction;

    public Transactional(EntityManager em, boolean joinTransaction) {
        this.em = em;
        this.joinTransaction = joinTransaction;
    }

    public Transactional(EntityManager em) {
        this(em, true);
    }

    public abstract void transact();
}
