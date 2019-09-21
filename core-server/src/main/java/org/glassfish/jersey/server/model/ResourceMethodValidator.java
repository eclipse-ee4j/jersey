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

package org.glassfish.jersey.server.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.sse.SseEventSink;

import org.glassfish.jersey.internal.Errors;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.glassfish.jersey.server.spi.internal.ParameterValueHelper;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

/**
 * Validator checking resource methods and sub resource locators. The validator mainly checks the parameters of resource
 * methods and sub resource locators.
 *
 * @author Miroslav Fuksa
 */
class ResourceMethodValidator extends AbstractResourceModelVisitor {

    private final Collection<ValueParamProvider> valueParamProviders;

    ResourceMethodValidator(Collection<ValueParamProvider> valueParamProviders) {
        this.valueParamProviders = valueParamProviders;
    }

    @Override
    public void visitResourceMethod(final ResourceMethod method) {
        switch (method.getType()) {
            case RESOURCE_METHOD:
                visitJaxrsResourceMethod(method);
                break;
            case SUB_RESOURCE_LOCATOR:
                visitSubResourceLocator(method);
                break;
        }

    }

    private void visitJaxrsResourceMethod(ResourceMethod method) {
        checkMethod(method);
    }

    private void checkMethod(ResourceMethod method) {
        checkValueProviders(method);
        final Invocable invocable = method.getInvocable();

        checkParameters(method);

        if ("GET".equals(method.getHttpMethod())) {
            final long eventSinkCount = invocable.getParameters()
                    .stream()
                    .filter(parameter -> SseEventSink.class.equals(parameter.getRawType()))
                    .count();

            final boolean isSse = eventSinkCount > 0;

            if (eventSinkCount > 1) {
                Errors.warning(method, LocalizationMessages.MULTIPLE_EVENT_SINK_INJECTION(invocable.getHandlingMethod()));
            }
            // ensure GET returns non-void value if not suspendable
            if (void.class == invocable.getHandlingMethod().getReturnType() && !method.isSuspendDeclared() && !isSse) {
                Errors.hint(method, LocalizationMessages.GET_RETURNS_VOID(invocable.getHandlingMethod()));
            }

            // ensure GET does not consume an entity parameter, if not inflector-based
            if (invocable.requiresEntity() && !invocable.isInflector()) {
                Errors.warning(method, LocalizationMessages.GET_CONSUMES_ENTITY(invocable.getHandlingMethod()));
            }
            // ensure GET does not consume any @FormParam annotated parameter
            for (Parameter p : invocable.getParameters()) {
                if (p.isAnnotationPresent(FormParam.class)) {
                    Errors.fatal(method, LocalizationMessages.GET_CONSUMES_FORM_PARAM(invocable.getHandlingMethod()));
                    break;
                }
            }

            if (isSse && void.class != invocable.getHandlingMethod().getReturnType()) {
                Errors.fatal(method, LocalizationMessages.EVENT_SINK_RETURNS_TYPE(invocable.getHandlingMethod()));
            }
        }

        // ensure there is not multiple HTTP method designators specified on the method
        List<String> httpMethodAnnotations = new LinkedList<>();
        for (Annotation a : invocable.getHandlingMethod().getDeclaredAnnotations()) {
            if (null != a.annotationType().getAnnotation(HttpMethod.class)) {
                httpMethodAnnotations.add(a.toString());
            }
        }

        if (httpMethodAnnotations.size() > 1) {
            Errors.fatal(method, LocalizationMessages.MULTIPLE_HTTP_METHOD_DESIGNATORS(invocable.getHandlingMethod(),
                    httpMethodAnnotations.toString()));
        }

        final Type responseType = invocable.getResponseType();
        if (!isConcreteType(responseType)) {
            Errors.warning(invocable.getHandlingMethod(),
                    LocalizationMessages.TYPE_OF_METHOD_NOT_RESOLVABLE_TO_CONCRETE_TYPE(responseType,
                            invocable.getHandlingMethod().toGenericString()));
        }

        final Path pathAnnotation = invocable.getHandlingMethod().getAnnotation(Path.class);
        if (pathAnnotation != null) {
            final String path = pathAnnotation.value();
            if (path == null || path.isEmpty() || "/".equals(path)) {

                Errors.warning(invocable.getHandlingMethod(),
                        LocalizationMessages.METHOD_EMPTY_PATH_ANNOTATION(
                                invocable.getHandlingMethod().getName(), invocable.getHandler().getHandlerClass().getName()));

            }
        }

        // Prevent PARAM_ANNOTATION_SET annotations on a resource method
        if (httpMethodAnnotations.size() != 0) {
            checkUnexpectedAnnotations(method);
        }
    }

    private void checkUnexpectedAnnotations(ResourceMethod resourceMethod) {
        Invocable invocable = resourceMethod.getInvocable();
        for (Annotation annotation : invocable.getHandlingMethod().getDeclaredAnnotations()) {
            if (PARAM_ANNOTATION_SET.contains(annotation.annotationType())) {
                Errors.fatal(resourceMethod, LocalizationMessages.METHOD_UNEXPECTED_ANNOTATION(
                        invocable.getHandlingMethod().getName(),
                        invocable.getHandler().getHandlerClass().getName(),
                        annotation.annotationType().getName())
                );
            }
        }
    }

    private void checkValueProviders(ResourceMethod method) {
        List<? extends Function<ContainerRequest, ?>> valueProviders =
                ParameterValueHelper.createValueProviders(valueParamProviders, method.getInvocable());
        if (valueProviders.contains(null)) {
            int index = valueProviders.indexOf(null);
            Errors.fatal(method, LocalizationMessages.ERROR_PARAMETER_MISSING_VALUE_PROVIDER(index, method.getInvocable()
                    .getHandlingMethod()));
        }
    }

    private void visitSubResourceLocator(ResourceMethod locator) {
        checkParameters(locator);
        checkValueProviders(locator);

        final Invocable invocable = locator.getInvocable();
        if (void.class == invocable.getRawResponseType()) {
            Errors.fatal(locator, LocalizationMessages.SUBRES_LOC_RETURNS_VOID(invocable.getHandlingMethod()));
        }

        // Prevent PARAM_ANNOTATION_SET annotations on a resource locator
        if (invocable.getHandlingMethod().getAnnotation(Path.class) != null) {
            checkUnexpectedAnnotations(locator);
        }
    }

    private void checkParameters(ResourceMethod method) {
        final Invocable invocable = method.getInvocable();
        final Method handlingMethod = invocable.getHandlingMethod();
        int paramCount = 0;
        int nonAnnotatedParameters = 0;

        for (Parameter p : invocable.getParameters()) {
            validateParameter(p, handlingMethod, handlingMethod.toGenericString(), Integer.toString(++paramCount), false);
            if (method.getType() == ResourceMethod.JaxrsType.SUB_RESOURCE_LOCATOR
                    && Parameter.Source.ENTITY == p.getSource()) {
                Errors.fatal(method, LocalizationMessages.SUBRES_LOC_HAS_ENTITY_PARAM(invocable.getHandlingMethod()));
            } else if (p.getAnnotations().length == 0) {
                nonAnnotatedParameters++;
                if (nonAnnotatedParameters > 1) {
                    Errors.fatal(method, LocalizationMessages.AMBIGUOUS_NON_ANNOTATED_PARAMETER(invocable.getHandlingMethod(),
                            invocable.getHandlingMethod().getDeclaringClass()));
                }
            }
        }
    }

    private boolean isSseInjected(final Invocable invocable) {
        return invocable.getParameters().stream().anyMatch(parameter -> SseEventSink.class.equals(parameter.getRawType()));
    }

    private static final Set<Class> PARAM_ANNOTATION_SET = createParamAnnotationSet();

    private static Set<Class> createParamAnnotationSet() {
        Set<Class> set = new HashSet<>(6);
        set.add(HeaderParam.class);
        set.add(CookieParam.class);
        set.add(MatrixParam.class);
        set.add(QueryParam.class);
        set.add(PathParam.class);
        set.add(BeanParam.class);
        return Collections.unmodifiableSet(set);
    }

    /**
     * Validate a single parameter instance.
     *
     * @param parameter             parameter to be validated.
     * @param source                parameter source; used for issue reporting.
     * @param reportedSourceName    source name; used for issue reporting.
     * @param reportedParameterName parameter name; used for issue reporting.
     * @param injectionsForbidden   {@code true} if parameters cannot be injected by
     *                              parameter annotations, eg. {@link HeaderParam @HeaderParam}.
     */
    static void validateParameter(final Parameter parameter,
                                  final Object source,
                                  final String reportedSourceName,
                                  final String reportedParameterName,
                                  final boolean injectionsForbidden) {
        Errors.processWithException(new Runnable() {
            @Override
            public void run() {
                int counter = 0;
                final Annotation[] annotations = parameter.getAnnotations();
                for (Annotation a : annotations) {
                    if (PARAM_ANNOTATION_SET.contains(a.annotationType())) {
                        if (injectionsForbidden) {
                            Errors.fatal(source, LocalizationMessages.SINGLETON_INJECTS_PARAMETER(reportedSourceName,
                                    reportedParameterName));
                            break;
                        }
                        counter++;
                        if (counter > 1) {
                            Errors.warning(source, LocalizationMessages.AMBIGUOUS_PARAMETER(reportedSourceName,
                                    reportedParameterName));
                            break;
                        }
                    }
                }

                final Type paramType = parameter.getType();
                if (!isConcreteType(paramType)) {
                    Errors.warning(source, LocalizationMessages.PARAMETER_UNRESOLVABLE(reportedParameterName, paramType,
                            reportedSourceName));
                }
            }
        });
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private static boolean isConcreteType(Type t) {
        if (t instanceof ParameterizedType) {
            return isConcreteParameterizedType((ParameterizedType) t);
        } else if (!(t instanceof Class)) {
            // GenericArrayType, WildcardType, TypeVariable
            return false;
        }

        return true;
    }

    private static boolean isConcreteParameterizedType(ParameterizedType pt) {
        boolean isConcrete = true;
        for (Type t : pt.getActualTypeArguments()) {
            isConcrete &= isConcreteType(t);
        }

        return isConcrete;
    }
}
