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

package org.glassfish.jersey.tests.cdi.resources;

import javax.annotation.PostConstruct;

/**
 * Basic timer implementation to be reused for various types of beans.
 *
 * @author Jakub Podlesak
 */
public abstract class BasicTimer {

    long ms;

   /**
    * Provide information on internal timer millisecond value.
    *
    * @return milliseconds when the current bean has been post-constructed.
    */
   public long getMiliseconds() {
       return ms;
   }

   /**
    * Initialize this timer with the current time.
    */
   @PostConstruct
   public void init() {
       this.ms = System.currentTimeMillis();
   }
}
