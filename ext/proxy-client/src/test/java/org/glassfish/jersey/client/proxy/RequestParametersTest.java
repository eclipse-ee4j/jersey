package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Richard Obersheimer
 */
public class RequestParametersTest {

    private static final MultivaluedMap<String, Object> EMPTY_HEADERS = new MultivaluedHashMap<>();
    private static final Form EMPTY_FORM = new Form();
    private static final String baseURL = "http://example.com";

    @QueryParam("queryParam")
    String queryParam;

    @QueryParam("queryParams")
    List<String> queryParams;

    @PathParam("pathParam")
    String pathParam;

    @HeaderParam("headerParam")
    String headerParam;

    @MatrixParam("matrixParam")
    List<String> matrixParam;

    @CookieParam("cookieParam")
    Cookie cookieParam;

    @BeanParam
    MySubBeanParam subBeanParam;

    @FormParam("formParam")
    String formParam;

    @FormParam("formParams")
    List<String> formParams;


    private WebTarget getExampleTarget() {
        Client client = ClientBuilder.newClient();
        return client.target(baseURL);
    }

    private WebTarget getExampleTargetWithPathParam() {
        Client client = ClientBuilder.newClient();
        return client.target(baseURL + "/{pathParam}");
    }

    private RequestParameters getEmptyRequestParameters(WebTarget webTarget) {
        return new RequestParameters(webTarget,
                EMPTY_HEADERS, Collections.emptyList(), EMPTY_FORM);
    }

    @Test
    public void testAddQueryParameter() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("queryParam").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(QueryParam.class, ann);

        requestParameters.addParameter("testQuery", anns);
        String uri = requestParameters.getNewTarget().getUriBuilder().build().toString();

        assertEquals(baseURL + "/?queryParam=testQuery", uri);
    }

    @Test
    public void testAddListOfQueryParameters() throws IntrospectionException, InvocationTargetException,
            IllegalAccessException, NoSuchFieldException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("queryParams").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(QueryParam.class, ann);
        List<String> subQueryParam = Arrays.asList("subQuery1", "subQuery2");

        requestParameters.addParameter(subQueryParam, anns);

        String uri = requestParameters.getNewTarget().getUriBuilder().build().toString();

        assertEquals(baseURL + "/?queryParams=subQuery1&queryParams=subQuery2", uri);
    }

    @Test
    public void testAddPathParameter() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTargetWithPathParam();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("pathParam").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(PathParam.class, ann);

        requestParameters.addParameter("testPath", anns);
        String uri = requestParameters.getNewTarget().getUriBuilder().build().toString();

        assertEquals(baseURL + "/testPath", uri);
    }

    @Test
    public void testAddHeaderParameter() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("headerParam").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(HeaderParam.class, ann);

        requestParameters.addParameter("testHeader", anns);
        MultivaluedHashMap<String, Object> headers = requestParameters.getHeaders();
        LinkedList<String> headerList = new LinkedList<>();
        headerList.add("testHeader");

        assertEquals(headerList, headers.get("headerParam"));
    }

    @Test
    public void testAddMatrixParameter() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("matrixParam").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(MatrixParam.class, ann);

        requestParameters.addParameter("testMatrix", anns);
        String uri = requestParameters.getNewTarget().getUriBuilder().build().toString();

        assertEquals(baseURL + "/;matrixParam=testMatrix", uri);
    }

    @Test
    public void testAddCookieParameter() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("cookieParam").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(CookieParam.class, ann);

        Cookie cookie = new Cookie("cookieParamName", "testCookie");
        requestParameters.addParameter(cookie, anns);
        List<Cookie> cookies = requestParameters.getCookies();

        assertEquals(new Cookie("cookieParam", "testCookie"), cookies.get(0));
    }

    @Test
    public void testAddFormParameter() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("formParam").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(FormParam.class, ann);

        requestParameters.addParameter("testForm", anns);
        Form form = requestParameters.getForm();
        LinkedList<String> formList = new LinkedList<>();
        formList.add("testForm");

        assertEquals(formList, form.asMap().get("formParam"));
    }

    @Test
    public void testListOfFormParameters() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        Annotation ann = this.getClass().getDeclaredField("formParams").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(FormParam.class, ann);

        List<String> testFormList = Arrays.asList("formParam1", "formParam2");
        requestParameters.addParameter(testFormList, anns);
        Form form = requestParameters.getForm();

        assertEquals(testFormList, form.asMap().get("formParams"));

    }

    // any nonempty annotation will do
    private Map<Class<?>, Annotation> getNonEmptyBeanParamAnnotation() throws NoSuchFieldException {
        Annotation ann = this.getClass().getDeclaredField("queryParam").getAnnotations()[0];
        Map<Class<?>, Annotation> anns = new HashMap<>();
        anns.put(BeanParam.class, ann);
        return anns;
    }

    @Test
    public void testAddBeanParameter() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        MyBeanParam beanParam = new MyBeanParam();
        beanParam.setQueryParam2("testQuery");

        Map<Class<?>, Annotation> anns = getNonEmptyBeanParamAnnotation();

        requestParameters.addParameter(beanParam, anns);
        String uri = requestParameters.getNewTarget().getUriBuilder().build().toString();

        assertEquals(baseURL + "/?queryParam2=testQuery", uri);
    }

    @Test
    public void testAddListOfBeanParameters() throws NoSuchFieldException, IntrospectionException,
            InvocationTargetException, IllegalAccessException {

        WebTarget webTarget = getExampleTarget();
        RequestParameters requestParameters = getEmptyRequestParameters(webTarget);

        MyBeanParam beanParam1 = new MyBeanParam();
        beanParam1.setQueryParam("testQuery");
        MyBeanParam beanParam2 = new MyBeanParam();
        beanParam2.setCookieParam(new Cookie("cookie", "cookieValue"));

        Map<Class<?>, Annotation> anns = getNonEmptyBeanParamAnnotation();

        List<MyBeanParam> beanParams = Arrays.asList(beanParam1, beanParam2);
        requestParameters.addParameter(beanParams, anns);

        String uri = requestParameters.getNewTarget().getUriBuilder().build().toString();
        List<Cookie> cookies = requestParameters.getCookies();

        assertEquals(baseURL + "/?queryParam=testQuery", uri);
        assertEquals(new Cookie("cookieParam", "cookieValue"), cookies.get(0));
    }
}