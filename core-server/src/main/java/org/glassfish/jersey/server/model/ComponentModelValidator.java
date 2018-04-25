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
package org.glassfish.jersey.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.jersey.Severity;
import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.message.MessageBodyWorkers;
import org.glassfish.jersey.server.model.internal.ModelErrors;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * A resource model validator that checks the given resource model.
 *
 * This base resource model validator class implements the visitor pattern to
 * traverse through all the {@link ResourceModelComponent resource model components}
 * to check validity of a resource model.
 * <p />
 * This validator maintains a list of all the {@link ResourceModelIssue issues}
 * found in the model. That way all the resource model components can be validated
 * in a single call to the {@link #validate(ResourceModelComponent) validate(...)}
 * method and collect all the validation issues from the model.
 * <p />
 * To check a single resource class, the the {@link Resource}
 * {@code builder(...)} can be used to create a resource model.
 *
 * {@link ComponentModelValidator#validate(ResourceModelComponent)}
 * method then populates the issue list, which could be then obtained by the
 * {@link ComponentModelValidator#getIssueList()}. Unless the list is explicitly cleared,
 * a subsequent calls to the validate method will add new items to the list,
 * so that it can be used to build the issue list for more than one resource. To clear the
 * list, the {@link ComponentModelValidator#cleanIssueList()} method should be called.
 * <p />
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public final class ComponentModelValidator {

    private final List<ResourceModelIssue> issueList = new LinkedList<>();

    public ComponentModelValidator(Collection<ValueParamProvider> valueParamProviders, MessageBodyWorkers msgBodyWorkers) {
        validators = new ArrayList<>();
        validators.add(new ResourceValidator());
        validators.add(new RuntimeResourceModelValidator(msgBodyWorkers));
        validators.add(new ResourceMethodValidator(valueParamProviders));
        validators.add(new InvocableValidator());
    }

    private final List<ResourceModelVisitor> validators;

    /**
     * Returns a list of issues found after
     * {@link #validate(org.glassfish.jersey.server.model.ResourceModelComponent)}
     * method has been invoked.
     *
     * @return a non-null list of issues.
     */
    public List<ResourceModelIssue> getIssueList() {
        return issueList;
    }

    /**
     * Convenience method to see if there were fatal issues found.
     *
     * @return {@code true} if there are any fatal issues present in the current
     *         issue list.
     */
    public boolean fatalIssuesFound() {
        for (ResourceModelIssue issue : getIssueList()) {
            if (issue.getSeverity() == Severity.FATAL) {
                return true;
            }
        }
        return false;
    }

    /**
     * Removes all issues from the current issue list. The method could be used
     * to re-use the same {@link ComponentModelValidator} for another resource model.
     */
    public void cleanIssueList() {
        issueList.clear();
    }

    /**
     * The validate method validates a component and adds possible
     * issues found to it's list. The list of issues could be then retrieved
     * via getIssueList method.
     *
     * @param component resource model component.
     */
    public void validate(final ResourceModelComponent component) {
        Errors.process(new Runnable() {
            @Override
            public void run() {
                Errors.mark();

                validateWithErrors(component);
                issueList.addAll(ModelErrors.getErrorsAsResourceModelIssues(true));

                Errors.unmark();
            }
        });
    }

    private void validateWithErrors(final ResourceModelComponent component) {
        for (ResourceModelVisitor validator : validators) {
            component.accept(validator);
        }

        final List<? extends ResourceModelComponent> componentList = component.getComponents();
        if (null != componentList) {
            for (ResourceModelComponent subComponent : componentList) {
                validateWithErrors(subComponent);
            }
        }
    }
}
