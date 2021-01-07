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

package org.glassfish.jersey.server.validation;


import org.glassfish.jersey.internal.util.ReflectionHelper;

/**
 * Validation error entity to be included in {@code Response} if JAX-B API is not available
 */
@SuppressWarnings("UnusedDeclaration")
public class ValidationErrorData {

    protected String message;

    protected String messageTemplate;

    protected String path;

    protected String invalidValue;

    /* package */ ValidationErrorData() {
    }

    /**
     * Create a {@code ValidationError} instance.
     *
     * @param message interpolated error message.
     * @param messageTemplate non-interpolated error message.
     * @param path property path.
     * @param invalidValue value that failed to pass constraints.
     */
    /* package */
    ValidationErrorData(final String message, final String messageTemplate, final String path, final String invalidValue) {
        this.message = message;
        this.messageTemplate = messageTemplate;
        this.path = path;
        this.invalidValue = invalidValue;
    }

    /**
     * Return the interpolated error message for this validation error.
     *
     * @return the interpolated error message for this validation error.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the interpolated error message for this validation error.
     *
     * @param message the interpolated error message for this validation error.
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * Return the string representation of the property path to the value.
     *
     * @return the string representation of the property path to the value.
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the string representation of the property path to the value.
     *
     * @param path the string representation of the property path to the value.
     */
    public void setPath(final String path) {
        this.path = path;
    }

    /**
     * Returns the string representation of the value failing to pass the constraint.
     *
     * @return the value failing to pass the constraint.
     */
    public String getInvalidValue() {
        return invalidValue;
    }

    /**
     * Set the value failing to pass the constraint.
     *
     * @param invalidValue the value failing to pass the constraint.
     */
    public void setInvalidValue(final String invalidValue) {
        this.invalidValue = invalidValue;
    }

    /**
     * Return the non-interpolated error message for this validation error.
     *
     * @return the non-interpolated error message for this validation error.
     */
    public String getMessageTemplate() {
        return messageTemplate;
    }

    /**
     * Set the non-interpolated error message for this validation error.
     *
     * @param messageTemplate the non-interpolated error message for this validation error.
     */
    public void setMessageTemplate(final String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    /*
     * Cache the information
     */
    private static Boolean isJaxbAvailable = null;

    private static boolean isJaxbAvailable() {
        if (isJaxbAvailable == null) {
            isJaxbAvailable = ReflectionHelper.isJaxbAvailable();
        }
        return isJaxbAvailable;
    }

    /**
     * A factory method that creates either JAX-B annotated data if JAX-B is available or POJO data otherwise.
     * @param message interpolated error message.
     * @param messageTemplate non-interpolated error message.
     * @param path property path.
     * @param invalidValue value that failed to pass constraints.
     * @return ValidationErrorData subclass or itself
     */
    public static ValidationErrorData createValidationError(
            final String message, final String messageTemplate, final String path, final String invalidValue) {
        if (isJaxbAvailable()) {
            return new ValidationError(message, messageTemplate, path, invalidValue);
        } else {
            return new ValidationErrorData(message, messageTemplate, path, invalidValue);
        }
    }
}
