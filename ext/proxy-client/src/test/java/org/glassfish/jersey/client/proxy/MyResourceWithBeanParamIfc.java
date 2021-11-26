package org.glassfish.jersey.client.proxy;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

/**
 * @author Richard Obersheimer
 */
@Path("mybeanresource")
public interface MyResourceWithBeanParamIfc {

    @GET
    @Path("getQuery")
    @Produces("text/plain")
    public String echoQuery(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getHeader")
    @Produces("text/plain")
    public String echoHeader(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getPath/{pathParam}")
    @Produces("text/plain")
    public String echoPath(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getCookie")
    @Produces("text/plain")
    public String echoCookie(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getMatrix")
    @Produces("text/plain")
    public String echoMatrix(@BeanParam MyGetBeanParam bean);

    @GET
    @Path("getSubBean")
    @Produces("text/plain")
    public String echoSubBean(@BeanParam MyGetBeanParam bean);

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("all/{pathParam}")
    @Produces("text/plain")
    public String echo(@BeanParam MyBeanParam bean);

    @Path("subresource")
    MyResourceWithBeanParamIfc getSubResource();

}
