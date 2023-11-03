/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.internal.deprecated;

import org.glassfish.jersey.internal.util.JdkVersion;

import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Class that wraps deprecated {@link AccessController} and {@link SecurityManager} methods.
 * @since 2.42
 */
public class ACDeprecator {

    @SuppressWarnings("deprecation")
    private static final SecurityManager SM = System.getSecurityManager();
    private static final boolean IS_SM = SM != null;

    /**
     * Returns true when security manager is set.
     */
    @SuppressWarnings("deprecation")
    public static boolean isSM() {
        return JdkVersion.getJdkVersion().getMajor() >= 17 ? IS_SM : System.getSecurityManager() != null;
    }

    /**
     * Returns true when security manager is set.
     */
    @SuppressWarnings("deprecation")
    public static SecurityManager getSM() {
        return JdkVersion.getJdkVersion().getMajor() >= 17 ? SM : System.getSecurityManager();
    }

    /**
     * Determines whether the access request indicated by the
     * specified permission should be allowed or denied.
     * @param permission the given {@link Permission}
     * @see AccessController#checkPermission(Permission)
     * @see SecurityManager#checkPermission(Permission)
     */
    public static void checkPermission(Permission permission) {
        if (isSM()) {
            AccessController.checkPermission(permission);
        }
    }

    /**
     * Performs the specified {@code PrivilegedAction} with privileges
     * enabled. The action is performed with <i>all</i> of the permissions
     * possessed by the caller's protection domain.
     *
     * <p> If the action's {@code run} method throws an (unchecked)
     * exception, it will propagate through this method.
     *
     * <p> Note that any {@code DomainCombiner} associated with the current
     * {@code AccessControlContext} will be ignored while the action is
     * performed.
     *
     * @param <T> the type of the value returned by the PrivilegedAction's
     *                  {@code run} method.
     *
     * @param action the action to be performed.
     *
     * @return the value returned by the action's {@code run} method.
     *
     * @throws    NullPointerException if the action is {@code null}
     * @see AccessController#doPrivileged(PrivilegedAction)
     */
    @SuppressWarnings("deprecation")
    public static <T> T doPrivilegedSM(PrivilegedAction<T> action) {
        return AccessController.doPrivileged(action);
    }

    /**
     * Checks whether the SecurityManager is available and if so,
     * performs the specified {@code PrivilegedAction} with privileges
     * enabled. The action is performed with <i>all</i> of the permissions
     * possessed by the caller's protection domain. Otherwise,
     * it just {@link PrivilegedAction#run runs} the action.
     *
     * <p> If the action's {@code run} method throws an (unchecked)
     * exception, it will propagate through this method.
     *
     * <p> Note that any {@code DomainCombiner} associated with the current
     * {@code AccessControlContext} will be ignored while the action is
     * performed.
     *
     * @param <T> the type of the value returned by the PrivilegedAction's
     *                  {@code run} method.
     *
     * @param action the action to be performed.
     *
     * @return the value returned by the action's {@code run} method.
     *
     * @throws    NullPointerException if the action is {@code null}
     */
    public static <T> T doPrivileged(PrivilegedAction<T> action) {
        return isSM() ? doPrivilegedSM(action) : action.run();
    }

    /**
     * Performs the specified {@code PrivilegedExceptionAction} with
     * privileges enabled.  The action is performed with <i>all</i> of the
     * permissions possessed by the caller's protection domain.
     *
     * <p> If the action's {@code run} method throws an <i>unchecked</i>
     * exception, it will propagate through this method.
     *
     * <p> Note that any {@code DomainCombiner} associated with the current
     * {@code AccessControlContext} will be ignored while the action is
     * performed.
     *
     * @param <T> the type of the value returned by the
     *                  PrivilegedExceptionAction's {@code run} method.
     *
     * @param action the action to be performed
     *
     * @return the value returned by the action's {@code run} method
     *
     * @throws PrivilegedActionException if the specified action's
     *         {@code run} method threw a <i>checked</i> exception
     * @throws    NullPointerException if the action is {@code null}
     * @see AccessController#doPrivileged(PrivilegedAction)
     */
    @SuppressWarnings("deprecation")
    public static <T> T doPrivilegedSM(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        return AccessController.doPrivileged(action);
    }

    /**
     * Checks whether the SecurityManager is present and
     * performs the specified {@code PrivilegedExceptionAction} with
     * privileges enabled.  The action is performed with <i>all</i> of the
     * permissions possessed by the caller's protection domain.
     * If not present, it just {@link PrivilegedExceptionAction#run() runs} the action.
     *
     * <p> If the action's {@code run} method throws an <i>unchecked</i>
     * exception, it will propagate through this method.
     *
     * <p> Note that any {@code DomainCombiner} associated with the current
     * {@code AccessControlContext} will be ignored while the action is
     * performed.
     *
     * @param <T> the type of the value returned by the
     *                  PrivilegedExceptionAction's {@code run} method.
     *
     * @param action the action to be performed
     *
     * @return the value returned by the action's {@code run} method
     *
     * @throws PrivilegedActionException if the specified action's
     *         {@code run} method threw a <i>checked</i> exception
     * @throws    NullPointerException if the action is {@code null}
     * @see AccessController#doPrivileged(PrivilegedAction)
     */
    public static <T> T doPrivileged(PrivilegedExceptionAction<T> action) throws PrivilegedActionException {
        try {
            return isSM() ? doPrivilegedSM(action) : action.run();
        } catch (Exception e) {
            throw new PrivilegedActionException(e);
        }
    }

}
