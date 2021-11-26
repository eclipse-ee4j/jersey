package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.core.Cookie;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Richard Obersheimer
 */
public class WebResourceFactoryBeanParamTest  extends JerseyTest {

    private MyResourceWithBeanParamIfc resourceWithBeanParam;

    @Override
    protected ResourceConfig configure() {
        enable(TestProperties.LOG_TRAFFIC);
        return new ResourceConfig(MyResourceWithBeanParam.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        resourceWithBeanParam = WebResourceFactory.newResource(MyResourceWithBeanParamIfc.class, target());
    }

    @Test
    public void testBeanParamQuery() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setQueryParam("query");

        String response = resourceWithBeanParam.echoQuery(myGetBeanParam);

        assertEquals("query", response);
    }

    @Test
    public void testBeanParamHeader() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setHeaderParam("header");

        String response = resourceWithBeanParam.echoHeader(myGetBeanParam);

        assertEquals("header", response);
    }

    @Test
    public void testBeanParamPath() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setPathParam("path");

        String response = resourceWithBeanParam.echoPath(myGetBeanParam);

        assertEquals("path", response);
    }

    @Test
    public void testBeanParamCookie() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        Cookie cookie = new Cookie("cName", "cValue");
        myGetBeanParam.setCookieParam(cookie);

        String response = resourceWithBeanParam.echoCookie(myGetBeanParam);

        assertEquals("cValue", response);
    }

    @Test
    public void testBeanParamMatrix() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        List<String> matrixParam = Arrays.asList("1", "2", "3");
        myGetBeanParam.setMatrixParam(matrixParam);

        String response = resourceWithBeanParam.echoMatrix(myGetBeanParam);

        assertEquals(matrixParam.toString(), response);
    }

    @Test
    public void testBeanParamSubBean() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        List<String> subQueryParam = Arrays.asList("1", "2", "3");
        MySubBeanParam subBeanParam = new MySubBeanParam(subQueryParam);
        myGetBeanParam.setSubBeanParam(subBeanParam);

        String response = resourceWithBeanParam.echoSubBean(myGetBeanParam);

        assertEquals(subQueryParam.toString(), response);
    }

    @Test
    public void testBeanParam() {
        List<String> matrixParam = Arrays.asList("1", "2", "3");
        Cookie cookieParam = new Cookie("cookie1", "value1");
        List<String> subQueryParam = Arrays.asList("subQuery1", "subQuery2");
        MySubBeanParam subBeanParam = new MySubBeanParam(subQueryParam);
        MyBeanParam myBeanParam = new MyBeanParam("header", "path", "query",
                "form1", "form2", matrixParam, cookieParam, subBeanParam);
        myBeanParam.setQueryParam2("q2");

        String response = resourceWithBeanParam.echo(myBeanParam);

        assertEquals("HEADER=header,PATH=path,FORM=form1,form2,QUERY=query,MATRIX=3,COOKIE=value1,SUB=2"
                + ",Q2=q2", response);
    }

    @Test
    public void testSubResource() {
        MyGetBeanParam myGetBeanParam = new MyGetBeanParam();
        myGetBeanParam.setQueryParam("query");

        String response = resourceWithBeanParam.getSubResource().echoQuery(myGetBeanParam);

        assertEquals("query", response);
    }
}
