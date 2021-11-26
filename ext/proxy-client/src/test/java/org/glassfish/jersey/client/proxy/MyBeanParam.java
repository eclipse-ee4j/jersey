package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Cookie;

import java.util.List;

/**
 * @author Richard Obersheimer
 */
public class MyBeanParam extends MyGetBeanParam {

    @FormParam("formParam1")
    String formParam1;

    @FormParam("formParam2")
    String formParam2;

    String queryParam2;

    public MyBeanParam(String headerParam, String pathParam, String queryParam, String formParam1, String formParam2,
                       List<String> matrixParam, Cookie cookieParam, MySubBeanParam subBeanParam) {
        this.headerParam = headerParam;
        this.pathParam = pathParam;
        this.queryParam = queryParam;
        this.formParam1 = formParam1;
        this.formParam2 = formParam2;
        this.matrixParam = matrixParam;
        this.cookieParam = cookieParam;
        this.subBeanParam = subBeanParam;
    }

    public MyBeanParam() {}

    public String getFormParam1() {
        return formParam1;
    }

    public void setFormParam1(String formParam1) {
        this.formParam1 = formParam1;
    }

    public String getFormParam2() {
        return formParam2;
    }

    public void setFormParam2(String formParam2) {
        this.formParam2 = formParam2;
    }

    @QueryParam("queryParam2")
    public String getQueryParam2() {
        return queryParam2;
    }

    @QueryParam("queryParam2")
    public void setQueryParam2(String queryParam2) {
        this.queryParam2 = queryParam2;
    }

}
