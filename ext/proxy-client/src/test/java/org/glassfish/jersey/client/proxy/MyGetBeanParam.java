package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.MatrixParam;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;

import java.util.List;

/**
 * @author Richard Obersheimer
 */
public class MyGetBeanParam {

    @HeaderParam("headerParam")
    String headerParam;

    @PathParam("pathParam")
    String pathParam;

    @QueryParam("queryParam")
    String queryParam;

    @MatrixParam("matrixParam")
    List<String> matrixParam;

    @CookieParam("cookieParam")
    Cookie cookieParam;

    @BeanParam
    MySubBeanParam subBeanParam;

    public MyGetBeanParam() {}

    public String getHeaderParam() {
        return headerParam;
    }

    public void setHeaderParam(String headerParam) {
        this.headerParam = headerParam;
    }

    public String getPathParam() {
        return pathParam;
    }

    public void setPathParam(String pathParam) {
        this.pathParam = pathParam;
    }

    public String getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(String queryParam) {
        this.queryParam = queryParam;
    }

    public List<String> getMatrixParam() {
        return matrixParam;
    }

    public void setMatrixParam(List<String> matrixParam) {
        this.matrixParam = matrixParam;
    }

    public Cookie getCookieParam() {
        return cookieParam;
    }

    public void setCookieParam(Cookie cookieParam) {
        this.cookieParam = cookieParam;
    }

    public MySubBeanParam getSubBeanParam() {
        return subBeanParam;
    }

    public void setSubBeanParam(MySubBeanParam subBeanParam) {
        this.subBeanParam = subBeanParam;
    }
}
