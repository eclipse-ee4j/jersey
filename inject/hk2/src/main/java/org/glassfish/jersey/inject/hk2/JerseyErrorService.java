/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.inject.hk2;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.Errors;

import org.glassfish.hk2.api.ErrorInformation;
import org.glassfish.hk2.api.ErrorService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Jersey implementation of HK2 Error Service to provide improved reporting
 * of HK2 issues, that may be otherwise hidden (ignored).
 *
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class JerseyErrorService implements ErrorService {

    /**
     * Binder for the Jersey implementation of HK2 {@link ErrorService} contract.
     */
    public static final class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(JerseyErrorService.class).to(ErrorService.class).in(Singleton.class);
        }
    }

    @Override
    public void onFailure(final ErrorInformation error) throws MultiException {
        final String msg;

        switch (error.getErrorType()) {
            case FAILURE_TO_REIFY:
                msg = LocalizationMessages.HK_2_REIFICATION_ERROR(
                        error.getDescriptor().getImplementation(), printStackTrace(error.getAssociatedException()));
                break;
            default:
                msg = LocalizationMessages.HK_2_UNKNOWN_ERROR(printStackTrace(error.getAssociatedException()));
                break;
        }

        try {
            Errors.warning(error.getInjectee(), msg);
        } catch (IllegalStateException ex) {
            Errors.process(new Runnable() {
                @Override
                public void run() {
                    Errors.warning(this, LocalizationMessages.HK_2_FAILURE_OUTSIDE_ERROR_SCOPE());
                    Errors.warning(error.getInjectee(), msg);
                }
            });
        }
    }

    private String printStackTrace(Throwable t) {
        final StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
