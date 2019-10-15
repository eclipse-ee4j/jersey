/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.microprofile.restclient;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.json.JsonValue;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

/**
 * Method model contains all information about method defined in rest client interface.
 *
 * @author David Kral
 * @author Patrik Dudits
 * @author Tomas Langer
 */
class MethodModel {

    private static final String INVOKED_METHOD = "org.eclipse.microprofile.rest.client.invokedMethod";

    private final InterfaceModel interfaceModel;

    private final Method method;
    private final GenericType<?> returnType;
    private final String httpMethod;
    private final String path;
    private final String[] produces;
    private final String[] consumes;
    private final List<ParamModel> parameterModels;
    private final List<ClientHeaderParamModel> clientHeaders;
    private final List<InterceptorInvocationContext.InvocationInterceptor> invocationInterceptors;
    private final RestClientModel subResourceModel;

    /**
     * Processes interface method and creates new instance of the model.
     *
     * @param interfaceModel
     * @param method
     * @return
     */
    static MethodModel from(InterfaceModel interfaceModel, Method method) {
        return new Builder(interfaceModel, method).build();
    }

    private MethodModel(Builder builder) {
        this.method = builder.method;
        this.interfaceModel = builder.interfaceModel;
        this.returnType = builder.returnType;
        this.httpMethod = builder.httpMethod;
        this.path = builder.pathValue;
        this.produces = builder.produces;
        this.consumes = builder.consumes;
        this.parameterModels = builder.parameterModels;
        this.clientHeaders = builder.clientHeaders;
        this.invocationInterceptors = builder.invocationInterceptors;
        if (httpMethod.isEmpty()) {
            subResourceModel = RestClientModel.from(returnType.getRawType(),
                                                    interfaceModel.getResponseExceptionMappers(),
                                                    interfaceModel.getParamConverterProviders(),
                                                    interfaceModel.getAsyncInterceptorFactories(),
                                                    interfaceModel.getInjectionManager(),
                                                    interfaceModel.getBeanManager());
        } else {
            subResourceModel = null;
        }
    }

    /**
     * Returns all registered cdi interceptors to this method.
     *
     * @return registered interceptors
     */
    List<InterceptorInvocationContext.InvocationInterceptor> getInvocationInterceptors() {
        return invocationInterceptors;
    }

    /**
     * Invokes corresponding method according to
     *
     * @param classLevelTarget
     * @param method
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    //I am checking the type of parameter and I know it should handle instance I am sending
    Object invokeMethod(WebTarget classLevelTarget, Method method, Object[] args) {
        WebTarget methodLevelTarget = classLevelTarget.path(path);

        AtomicReference<Object> entity = new AtomicReference<>();
        AtomicReference<WebTarget> webTargetAtomicReference = new AtomicReference<>(methodLevelTarget);
        parameterModels.stream()
                .filter(parameterModel -> parameterModel.handles(PathParam.class))
                .forEach(parameterModel ->
                                 webTargetAtomicReference.set((WebTarget)
                                                                      parameterModel
                                                                              .handleParameter(webTargetAtomicReference.get(),
                                                                                               PathParam.class,
                                                                                               args[parameterModel
                                                                                                       .getParamPosition()])));

        parameterModels.stream()
                .filter(ParamModel::isEntity)
                .findFirst()
                .ifPresent(parameterModel -> entity.set(args[parameterModel.getParamPosition()]));

        Form form = handleForm(args);

        WebTarget webTarget = webTargetAtomicReference.get();
        if (httpMethod.isEmpty()) {
            //sub resource method
            return subResourceProxy(webTarget, returnType.getRawType());
        }
        webTarget = addQueryParams(webTarget, args);
        webTarget = addMatrixParams(webTarget, args);

        MultivaluedMap<String, Object> customHeaders = addCustomHeaders(args);

        Object entityToUse = entity.get();
        if (entityToUse == null && !form.asMap().isEmpty()) {
            entityToUse = form;
        }
        if (entityToUse == null) {
            customHeaders.remove(HttpHeaders.CONTENT_TYPE);
        }

        Invocation.Builder builder = webTarget
                .request(produces)
                .property(INVOKED_METHOD, method)
                .headers(customHeaders);
        builder = addCookies(builder, args);

        Object response;

        if (CompletionStage.class.isAssignableFrom(method.getReturnType())) {
            response = asynchronousCall(builder, entityToUse, method, customHeaders);
        } else {
            response = synchronousCall(builder, entityToUse, method, customHeaders);
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    private Form handleForm(Object[] args) {
        final Form form = new Form();
        parameterModels.stream()
                .filter(parameterModel -> parameterModel.handles(FormParam.class))
                .forEach(parameterModel -> parameterModel.handleParameter(form,
                                                                          FormParam.class,
                                                                          args[parameterModel.getParamPosition()]));
        return form;
    }

    private Object synchronousCall(Invocation.Builder builder,
                                   Object entity,
                                   Method method,
                                   MultivaluedMap<String, Object> customHeaders) {
        Response response;

        if (entity != null
                && !httpMethod.equals(GET.class.getSimpleName())
                && !httpMethod.equals(DELETE.class.getSimpleName())) {
            response = builder.method(httpMethod, Entity.entity(entity, getContentType(customHeaders)));
        } else {
            response = builder.method(httpMethod);
        }

        evaluateResponse(response, method);

        if (returnType.getType().equals(Void.class)) {
            return null;
        } else if (returnType.getType().equals(Response.class)) {
            return response;
        }
        return response.readEntity(returnType);
    }

    private CompletableFuture asynchronousCall(Invocation.Builder builder,
                                               Object entity,
                                               Method method,
                                               MultivaluedMap<String, Object> customHeaders) {

        //AsyncInterceptors initialization
        List<AsyncInvocationInterceptor> asyncInterceptors = interfaceModel.getAsyncInterceptorFactories().stream()
                .map(AsyncInvocationInterceptorFactory::newInterceptor)
                .collect(Collectors.toList());
        asyncInterceptors.forEach(AsyncInvocationInterceptor::prepareContext);
        ExecutorServiceWrapper.asyncInterceptors.set(asyncInterceptors);

        CompletableFuture<Object> result = new CompletableFuture<>();
        Future<Response> theFuture;
        if (entity != null
                && !httpMethod.equals(GET.class.getSimpleName())
                && !httpMethod.equals(DELETE.class.getSimpleName())) {
            theFuture = builder.async().method(httpMethod, Entity.entity(entity, getContentType(customHeaders)));
        } else {
            theFuture = builder.async().method(httpMethod);
        }

        CompletableFuture<Response> completableFuture = (CompletableFuture<Response>) theFuture;
        completableFuture.thenAccept(response -> {
            asyncInterceptors.forEach(AsyncInvocationInterceptor::removeContext);
            try {
                evaluateResponse(response, method);
                if (returnType.getType().equals(Void.class)) {
                    result.complete(null);
                } else if (returnType.getType().equals(Response.class)) {
                    result.complete(response);
                } else {
                    result.complete(response.readEntity(returnType));
                }
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        }).exceptionally(throwable -> {
            // Since it could have been the removeContext method causing exception, we need to be more careful
            // to assure, that the future completes
            asyncInterceptors.forEach(interceptor -> {
                try {
                    interceptor.removeContext();
                } catch (Throwable e) {
                    throwable.addSuppressed(e);
                }
            });
            result.completeExceptionally(throwable);
            return null;
        });

        return result;
    }

    private String getContentType(MultivaluedMap<String, Object> customHeaders) {
        return (String) customHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
    }

    @SuppressWarnings("unchecked")
    private <T> T subResourceProxy(WebTarget webTarget, Class<T> subResourceType) {
        return (T) Proxy.newProxyInstance(subResourceType.getClassLoader(),
                                          new Class[] {subResourceType},
                                          new ProxyInvocationHandler(webTarget, subResourceModel)
        );
    }

    @SuppressWarnings("unchecked") //I am checking the type of parameter and I know it should handle instance I am sending
    private WebTarget addQueryParams(WebTarget webTarget, Object[] args) {
        Map<String, Object[]> queryParams = new HashMap<>();
        WebTarget toReturn = webTarget;
        parameterModels.stream()
                .filter(parameterModel -> parameterModel.handles(QueryParam.class))
                .forEach(parameterModel -> parameterModel.handleParameter(queryParams,
                                                                          QueryParam.class,
                                                                          args[parameterModel.getParamPosition()]));

        for (Map.Entry<String, Object[]> entry : queryParams.entrySet()) {
            toReturn = toReturn.queryParam(entry.getKey(), entry.getValue());
        }
        return toReturn;
    }

    @SuppressWarnings("unchecked") //I am checking the type of parameter and I know it should handle instance I am sending
    private WebTarget addMatrixParams(WebTarget webTarget, Object[] args) {
        AtomicReference<WebTarget> toReturn = new AtomicReference<>(webTarget);
        parameterModels.stream()
                .filter(parameterModel -> parameterModel.handles(MatrixParam.class))
                .forEach(parameterModel -> toReturn
                        .set((WebTarget) parameterModel.handleParameter(toReturn.get(),
                                                                        MatrixParam.class,
                                                                        args[parameterModel.getParamPosition()])));
        return toReturn.get();
    }

    @SuppressWarnings("unchecked") //I am checking the type of parameter and I know it should handle instance I am sending
    private Invocation.Builder addCookies(Invocation.Builder builder, Object[] args) {
        Map<String, String> cookies = new HashMap<>();
        Invocation.Builder toReturn = builder;
        parameterModels.stream()
                .filter(parameterModel -> parameterModel.handles(CookieParam.class))
                .forEach(parameterModel -> parameterModel.handleParameter(cookies,
                                                                          CookieParam.class,
                                                                          args[parameterModel.getParamPosition()]));

        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            toReturn = toReturn.cookie(entry.getKey(), entry.getValue());
        }
        return toReturn;
    }

    private MultivaluedMap<String, Object> addCustomHeaders(Object[] args) {
        MultivaluedMap<String, Object> result = new MultivaluedHashMap<>();
        for (Map.Entry<String, List<String>> entry : resolveCustomHeaders(args).entrySet()) {
            entry.getValue().forEach(val -> result.add(entry.getKey(), val));
        }
        for (String produce : produces) {
            result.add(HttpHeaders.ACCEPT, produce);
        }
        result.putIfAbsent(HttpHeaders.CONTENT_TYPE, Collections.singletonList(consumes[0]));
        return new MultivaluedHashMap<String, Object>(result);
    }

    @SuppressWarnings("unchecked") //I am checking the type of parameter and I know it should handle instance I am sending
    private MultivaluedMap<String, String> resolveCustomHeaders(Object[] args) {
        MultivaluedMap<String, String> customHeaders = new MultivaluedHashMap<>();
        customHeaders.putAll(createMultivaluedHeadersMap(interfaceModel.getClientHeaders()));
        customHeaders.putAll(createMultivaluedHeadersMap(clientHeaders));
        parameterModels.stream()
                .filter(parameterModel -> parameterModel.handles(HeaderParam.class))
                .forEach(parameterModel -> parameterModel.handleParameter(customHeaders,
                                                                          HeaderParam.class,
                                                                          args[parameterModel.getParamPosition()]));

        MultivaluedMap<String, String> inbound = new MultivaluedHashMap<>();
        HeadersContext.get().ifPresent(headersContext -> inbound.putAll(headersContext.inboundHeaders()));

        AtomicReference<MultivaluedMap<String, String>> toReturn = new AtomicReference<>(customHeaders);
        interfaceModel.getClientHeadersFactory().ifPresent(clientHeadersFactory -> toReturn
                .set(clientHeadersFactory.update(inbound, customHeaders)));
        return toReturn.get();
    }

    private <T> MultivaluedMap<String, String> createMultivaluedHeadersMap(List<ClientHeaderParamModel> clientHeaders) {
        MultivaluedMap<String, String> customHeaders = new MultivaluedHashMap<>();
        for (ClientHeaderParamModel clientHeaderParamModel : clientHeaders) {
            if (clientHeaderParamModel.getComputeMethod() == null) {
                customHeaders
                        .put(clientHeaderParamModel.getHeaderName(), Arrays.asList(clientHeaderParamModel.getHeaderValue()));
            } else {
                try {
                    Method method = clientHeaderParamModel.getComputeMethod();
                    if (method.isDefault()) {
                        //method is interface default
                        //we need to create instance of the interface to be able to call default method
                        T instance = (T) ReflectionUtil.createProxyInstance(interfaceModel.getRestClientClass());
                        if (method.getParameterCount() > 0) {
                            customHeaders.put(clientHeaderParamModel.getHeaderName(),
                                              createList(method.invoke(instance, clientHeaderParamModel.getHeaderName())));
                        } else {
                            customHeaders.put(clientHeaderParamModel.getHeaderName(),
                                              createList(method.invoke(instance, null)));
                        }
                    } else {
                        //Method is static
                        if (method.getParameterCount() > 0) {
                            customHeaders.put(clientHeaderParamModel.getHeaderName(),
                                              createList(method.invoke(null, clientHeaderParamModel.getHeaderName())));
                        } else {
                            customHeaders.put(clientHeaderParamModel.getHeaderName(),
                                              createList(method.invoke(null, null)));
                        }
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (InvocationTargetException e) {
                    if (clientHeaderParamModel.isRequired()) {
                        if (e.getCause() instanceof RuntimeException) {
                            throw (RuntimeException) e.getCause();
                        }
                        throw new RuntimeException(e.getCause());
                    }
                }
            }
        }
        return customHeaders;
    }

    private static List<String> createList(Object value) {
        if (value instanceof String[]) {
            String[] array = (String[]) value;
            return Arrays.asList(array);
        }
        String s = (String) value;
        return Collections.singletonList(s);
    }

    /**
     * Evaluation of {@link Response} if it is applicable for any of the registered {@link ResponseExceptionMapper} providers.
     *
     * @param response obtained response
     * @param method   called method
     */
    private void evaluateResponse(Response response, Method method) {
        ResponseExceptionMapper lowestMapper = null;
        Throwable throwable = null;
        for (ResponseExceptionMapper responseExceptionMapper : interfaceModel.getResponseExceptionMappers()) {
            if (responseExceptionMapper.handles(response.getStatus(), response.getHeaders())) {
                if (lowestMapper == null
                        || throwable == null
                        || lowestMapper.getPriority() > responseExceptionMapper.getPriority()) {
                    lowestMapper = responseExceptionMapper;
                    Throwable tmp = lowestMapper.toThrowable(response);
                    if (tmp != null) {
                        throwable = tmp;
                    }
                }
            }
        }
        if (throwable != null) {
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            } else if (throwable instanceof Error) {
                throw (Error) throwable;
            }
            for (Class<?> exception : method.getExceptionTypes()) {
                if (throwable.getClass().isAssignableFrom(exception)) {
                    throw new WebApplicationException(throwable);
                }
            }
        }
    }

    private static String parseHttpMethod(InterfaceModel classModel, Method method) {
        List<Class<?>> httpAnnotations = InterfaceUtil.getHttpAnnotations(method);
        if (httpAnnotations.size() > 1) {
            throw new RestClientDefinitionException("Method can't have more then one annotation of @HttpMethod type. "
                                                            + "See " + classModel.getRestClientClass().getName()
                                                            + "::" + method.getName());
        }
        if (httpAnnotations.isEmpty()) {
            //Sub resource method
            return "";
        }
        return httpAnnotations.get(0).getSimpleName();
    }

    private static List<ParamModel> parameterModels(InterfaceModel classModel, Method method) {
        List<ParamModel> parameterModels = new ArrayList<>();
        final List<org.glassfish.jersey.model.Parameter> jerseyParameters = org.glassfish.jersey.model.Parameter
                .create(classModel.getRestClientClass(), classModel.getRestClientClass(),
                        method, false);
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            parameterModels.add(ParamModel.from(classModel, parameters[i].getType(), parameters[i], jerseyParameters.get(i), i));
        }
        return parameterModels;
    }

    private static class Builder {

        private final InterfaceModel interfaceModel;
        private final Method method;

        private GenericType<?> returnType;
        private String httpMethod;
        private String pathValue;
        private String[] produces;
        private String[] consumes;
        private List<ParamModel> parameterModels;
        private List<ClientHeaderParamModel> clientHeaders;
        private List<InterceptorInvocationContext.InvocationInterceptor> invocationInterceptors;

        private Builder(InterfaceModel interfaceModel, Method method) {
            this.interfaceModel = interfaceModel;
            this.method = method;
            filterAllInterceptorAnnotations();
        }

        private void filterAllInterceptorAnnotations() {
            invocationInterceptors = new ArrayList<>();
            BeanManager beanManager = interfaceModel.getBeanManager();
            if (beanManager != null) {
                Set<Annotation> interceptorAnnotations = new HashSet<>();
                for (Annotation annotation : method.getAnnotations()) {
                    if (beanManager.isInterceptorBinding(annotation.annotationType())) {
                        interceptorAnnotations.add(annotation);
                    }
                }
                interceptorAnnotations.addAll(interfaceModel.getInterceptorAnnotations());
                Annotation[] allInterceptorAnnotations = interceptorAnnotations.toArray(new Annotation[0]);
                if (allInterceptorAnnotations.length == 0) {
                    return;
                }
                List<Interceptor<?>> interceptors = beanManager.resolveInterceptors(InterceptionType.AROUND_INVOKE,
                                                                                    allInterceptorAnnotations);
                if (!interceptors.isEmpty()) {
                    for (Interceptor<?> interceptor : interceptors) {
                        Object interceptorInstance = beanManager.getReference(interceptor,
                                                                              interceptor.getBeanClass(),
                                                                              interfaceModel.getCreationalContext());
                        invocationInterceptors.add(new InterceptorInvocationContext
                                .InvocationInterceptor(interceptorInstance,
                                                       interceptor));
                    }
                }
            }
        }

        /**
         * Return type of the method.
         *
         * @param returnType Method return type
         */
        private void returnType(Type returnType) {
            if (returnType instanceof ParameterizedType
                    && CompletionStage.class.isAssignableFrom((Class<?>) ((ParameterizedType) returnType).getRawType())) {
                this.returnType = new GenericType<>(((ParameterizedType) returnType).getActualTypeArguments()[0]);
            } else {
                this.returnType = new GenericType<>(returnType);
            }
        }

        /**
         * HTTP method of the method.
         *
         * @param httpMethod HTTP method of the method
         */
        private void httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        /**
         * Path value from {@link Path} annotation. If annotation is null, empty String is set as path.
         *
         * @param path {@link Path} annotation
         */
        private void pathValue(Path path) {
            this.pathValue = path != null ? path.value() : "";
            //if only / is added to path like this "localhost:80/test" it makes invalid path "localhost:80/test/"
            this.pathValue = pathValue.equals("/") ? "" : pathValue;
        }

        /**
         * Extracts MediaTypes from {@link Produces} annotation.
         * If annotation is null, value from {@link InterfaceModel} is set.
         *
         * @param produces {@link Produces} annotation
         */
        private void produces(Produces produces) {
            this.produces = produces == null ? interfaceModel.getProduces() : produces.value();
        }

        /**
         * Extracts MediaTypes from {@link Consumes} annotation.
         * If annotation is null, value from {@link InterfaceModel} is set.
         *
         * @param consumes {@link Consumes} annotation
         */
        private void consumes(Consumes consumes) {
            this.consumes = consumes == null ? interfaceModel.getConsumes() : consumes.value();
        }

        /**
         * {@link List} of transformed method parameters.
         *
         * @param parameterModels {@link List} of parameters
         */
        private void parameters(List<ParamModel> parameterModels) {
            this.parameterModels = parameterModels;
        }

        /**
         * Process data from {@link ClientHeaderParam} annotation to extract methods and values.
         *
         * @param clientHeaderParams {@link ClientHeaderParam} annotations
         */
        private void clientHeaders(ClientHeaderParam[] clientHeaderParams) {
            clientHeaders = Arrays.stream(clientHeaderParams)
                    .map(clientHeaderParam -> new ClientHeaderParamModel(interfaceModel.getRestClientClass(), clientHeaderParam))
                    .collect(Collectors.toList());
        }

        /**
         * Creates new MethodModel instance.
         *
         * @return new instance
         */
        MethodModel build() {
            returnType(method.getGenericReturnType());
            httpMethod(parseHttpMethod(interfaceModel, method));
            pathValue(method.getAnnotation(Path.class));
            produces(method.getAnnotation(Produces.class));
            consumes(method.getAnnotation(Consumes.class));
            parameters(parameterModels(interfaceModel, method));
            clientHeaders(method.getAnnotationsByType(ClientHeaderParam.class));

            validateParameters();
            validateHeaderDuplicityNames();

            if (isJsonValue(returnType.getType())) {
                this.produces = new String[] {MediaType.APPLICATION_JSON};
            }

            parameterModels.stream()
                    .filter(ParamModel::isEntity)
                    .map(ParamModel::getType)
                    .filter(this::isJsonValue)
                    .findFirst()
                    .ifPresent(paramModel -> this.consumes = new String[] {MediaType.APPLICATION_JSON});

            return new MethodModel(this);
        }

        private void validateParameters() {
            UriBuilder uriBuilder = UriBuilder.fromUri(interfaceModel.getPath()).path(pathValue);
            List<String> parameters = InterfaceUtil.parseParameters(uriBuilder.toTemplate());
            List<String> methodPathParameters = new ArrayList<>();
            List<ParamModel> pathHandlingParams = parameterModels.stream()
                    .filter(parameterModel -> parameterModel.handles(PathParam.class))
                    .collect(Collectors.toList());
            for (ParamModel paramModel : pathHandlingParams) {
                if (paramModel instanceof PathParamModel) {
                    methodPathParameters.add(((PathParamModel) paramModel).getPathParamName());
                } else if (paramModel instanceof BeanParamModel) {
                    for (ParamModel beanPathParams : ((BeanParamModel) paramModel).getAllParamsWithType(PathParam.class)) {
                        methodPathParameters.add(((PathParamModel) beanPathParams).getPathParamName());
                    }
                }
            }
            for (String parameterName : methodPathParameters) {
                if (!parameters.contains(parameterName)) {
                    throw new RestClientDefinitionException("Parameter name " + parameterName + " on "
                                                                    + interfaceModel.getRestClientClass().getName()
                                                                    + "::" + method.getName()
                                                                    + " doesn't match any @Path variable name.");
                }
                parameters.remove(parameterName);
            }
            if (!parameters.isEmpty()) {
                throw new RestClientDefinitionException("Some variable names does not have matching @PathParam "
                                                                + "defined on method " + interfaceModel.getRestClientClass()
                        .getName()
                                                                + "::" + method.getName());
            }
            List<ParamModel> entities = parameterModels.stream()
                    .filter(ParamModel::isEntity)
                    .collect(Collectors.toList());
            if (entities.size() > 1) {
                throw new RestClientDefinitionException("You cant have more than 1 entity method parameter! Check "
                                                                + interfaceModel.getRestClientClass().getName()
                                                                + "::" + method.getName());
            }
        }

        private void validateHeaderDuplicityNames() {
            ArrayList<String> names = new ArrayList<>();
            for (ClientHeaderParamModel clientHeaderParamModel : clientHeaders) {
                String headerName = clientHeaderParamModel.getHeaderName();
                if (names.contains(headerName)) {
                    throw new RestClientDefinitionException("Header name cannot be registered more then once on the same target."
                                                                    + "See " + interfaceModel.getRestClientClass().getName());
                }
                names.add(headerName);
            }
        }

        private boolean isJsonValue(Type type) {
            return type instanceof Class && JsonValue.class.isAssignableFrom((Class<?>) type);
        }
    }
}
