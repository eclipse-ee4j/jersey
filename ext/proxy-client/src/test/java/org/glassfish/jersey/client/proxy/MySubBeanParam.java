package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.QueryParam;

import java.util.List;

/**
 * @author Richard Obersheimer
 */
public class MySubBeanParam {

    public List<String> getSubQueryParam() {
        return subQueryParam;
    }

    public void setSubQueryParam(List<String> subQueryParam) {
        this.subQueryParam = subQueryParam;
    }

    public MySubBeanParam(List<String> subQueryParam) {
        this.subQueryParam = subQueryParam;
    }

    public MySubBeanParam() {}

    @QueryParam("subQueryParam")
    List<String> subQueryParam;
}
