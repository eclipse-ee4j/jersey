/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server;

import java.lang.annotation.Annotation;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * An abstract extension of {@link WebApplicationException} for the class of
 * parameter-based exceptions.
 * <p>
 * Exceptions that are instances of this class will be thrown if the runtime
 * encounters an error obtaining a parameter value, from a request, for a
 * Java type that is annotated with a parameter-based annotation, such as
 * {@link QueryParam}. For more details see
 * <a href="http://jsr311.java.net/nonav/releases/1.0/spec/index.html">section 3.2</a>
 * of the JAX-RS specification.
 * <p>
 * An {@link ExceptionMapper} may be configured to map this class or a sub-class
 * of to customize responses for parameter-based errors.
 * <p>
 * Unless otherwise stated all such exceptions of this type will contain a
 * response with a 400 (Client error) status code.
 *
 * @author Paul Sandoz
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public abstract class ParamException extends WebApplicationException {

    private static final long serialVersionUID = -2359567574412607846L;

    /**
     * An abstract parameter exception for the class of URI-parameter-based
     * exceptions.
     * <p>
     * All such exceptions of this type will contain a response with a 404
     * (Not Found) status code.
     */
    public abstract static class UriParamException extends ParamException {

        private static final long serialVersionUID = 44233528459885541L;

        protected UriParamException(Throwable cause,
                                    Class<? extends Annotation> parameterType, String name, String defaultStringValue) {
            super(cause, Response.Status.NOT_FOUND, parameterType, name, defaultStringValue);
        }
    }

    /**
     * A URI-parameter-based exception for errors with {@link PathParam}.
     */
    public static class PathParamException extends UriParamException {

        private static final long serialVersionUID = -2708538214692835633L;

        public PathParamException(Throwable cause, String name, String defaultStringValue) {
            super(cause, PathParam.class, name, defaultStringValue);
        }
    }

    /**
     * A URI-parameter-based exception for errors with {@link MatrixParam}.
     */
    public static class MatrixParamException extends UriParamException {

        private static final long serialVersionUID = -5849392883623736362L;

        public MatrixParamException(Throwable cause, String name, String defaultStringValue) {
            super(cause, MatrixParam.class, name, defaultStringValue);
        }
    }

    /**
     * A URI-parameter-based exception for errors with {@link QueryParam}.
     */
    public static class QueryParamException extends UriParamException {

        private static final long serialVersionUID = -4822407467792322910L;

        public QueryParamException(Throwable cause, String name, String defaultStringValue) {
            super(cause, QueryParam.class, name, defaultStringValue);
        }
    }

    /**
     * A parameter exception for errors with {@link HeaderParam}.
     */
    public static class HeaderParamException extends ParamException {

        private static final long serialVersionUID = 6508174603506313274L;

        public HeaderParamException(Throwable cause, String name, String defaultStringValue) {
            super(cause, Response.Status.BAD_REQUEST, HeaderParam.class, name, defaultStringValue);
        }
    }

    /**
     * A parameter exception for errors with {@link CookieParam}.
     */
    public static class CookieParamException extends ParamException {

        private static final long serialVersionUID = -5288504201234567266L;

        public CookieParamException(Throwable cause, String name, String defaultStringValue) {
            super(cause, Response.Status.BAD_REQUEST, CookieParam.class, name, defaultStringValue);
        }
    }

    /**
     * A parameter exception for errors with {@link FormParam}.
     */
    public static class FormParamException extends ParamException {

        private static final long serialVersionUID = -1704379792199980689L;

        public FormParamException(Throwable cause, String name, String defaultStringValue) {
            super(cause, Response.Status.BAD_REQUEST, FormParam.class, name, defaultStringValue);
        }
    }

    private final Class<? extends Annotation> parameterType;
    private final String name;
    private final String defaultStringValue;

    protected ParamException(Throwable cause, Response.StatusType status,
                             Class<? extends Annotation> parameterType, String name, String defaultStringValue) {
        super(cause, status.getStatusCode());
        this.parameterType = parameterType;
        this.name = name;
        this.defaultStringValue = defaultStringValue;
    }

    /**
     * Get the type of the parameter annotation.
     *
     * @return the type of the parameter annotation.
     */
    public Class<? extends Annotation> getParameterType() {
        return parameterType;
    }

    /**
     * Get the parameter name.
     *
     * @return the parameter name.
     */
    public String getParameterName() {
        return name;
    }

    /**
     * Get the default String value.
     *
     * @return the default String value.
     */
    public String getDefaultStringValue() {
        return defaultStringValue;
    }
}
