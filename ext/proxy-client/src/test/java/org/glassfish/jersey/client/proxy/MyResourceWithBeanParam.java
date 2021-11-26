package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.BeanParam;

/**
 * @author Richard Obersheimer
 */
public class MyResourceWithBeanParam implements MyResourceWithBeanParamIfc {

    @Override
    public String echoQuery(MyGetBeanParam bean) {
        return bean.getQueryParam();
    }

    @Override
    public String echoHeader(@BeanParam MyGetBeanParam bean) {
        return bean.getHeaderParam();
    }

    @Override
    public String echoPath(@BeanParam MyGetBeanParam bean) {
        return bean.getPathParam();
    }

    @Override
    public String echoCookie(@BeanParam MyGetBeanParam bean) {
        return bean.getCookieParam().getValue();
    }

    @Override
    public String echoMatrix(@BeanParam MyGetBeanParam bean) {
        return bean.getMatrixParam().toString();
    }

    @Override
    public String echoSubBean(@BeanParam MyGetBeanParam bean) {
        return bean.getSubBeanParam().getSubQueryParam().toString();
    }

    @Override
    public String echo(MyBeanParam bean) {
        return ("HEADER=" + bean.getHeaderParam() + ",PATH=" + bean.getPathParam() + ",FORM="
                + bean.getFormParam1() + "," + bean.getFormParam2() + ",QUERY=" + bean.getQueryParam()
                + ",MATRIX=" + bean.getMatrixParam().size() + ",COOKIE=" + bean.getCookieParam().getValue()
                + ",SUB=" + bean.getSubBeanParam().getSubQueryParam().size()
                + ",Q2=" + bean.getQueryParam2());
    }

    @Override
    public MyResourceWithBeanParamIfc getSubResource() {
        return new MyResourceWithBeanParam();
    }
}
