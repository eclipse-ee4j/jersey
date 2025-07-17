/*
 * Copyright (c) 2016, 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.netty.connector;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientRequest;
import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.http.HttpHeaders;
import org.glassfish.jersey.http.ResponseStatus;
import org.glassfish.jersey.netty.connector.internal.NettyInputStream;
import org.glassfish.jersey.netty.connector.internal.RedirectException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;
import org.glassfish.jersey.uri.internal.JerseyUriBuilder;

/**
 * Jersey implementation of Netty channel handler.
 *
 * @author Pavel Bucek
 */
class JerseyClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    // Modified only by the same thread. No need to synchronize it.
    private final Set<URI> redirectUriHistory;
    private final ClientRequest jerseyRequest;
    private final CompletableFuture<ClientResponse> responseAvailable;
    private final CompletableFuture<?> responseDone;
    private final NettyConnector connector;
    private final NettyHttpRedirectController redirectController;
    private final NettyConnectorProvider.Config.RW requestConfiguration;

    private NettyInputStream nis;
    private ClientResponse jerseyResponse;

    private boolean readTimedOut;

    JerseyClientHandler(ClientRequest request, CompletableFuture<ClientResponse> responseAvailable,
                        CompletableFuture<?> responseDone, Set<URI> redirectUriHistory, NettyConnector connector,
                        NettyConnectorProvider.Config.RW requestConfiguration) {
        this.redirectUriHistory = redirectUriHistory;
        this.jerseyRequest = request;
        this.responseAvailable = responseAvailable;
        this.responseDone = responseDone;
        this.requestConfiguration = requestConfiguration;
        this.connector = connector;
        // Follow redirects by default
        requestConfiguration.followRedirects(jerseyRequest);
        requestConfiguration.maxRedirects(jerseyRequest);

        this.redirectController = requestConfiguration.redirectController(jerseyRequest);
        this.redirectController.init(requestConfiguration);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
       notifyResponse(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
       // assert: no-op, if channel is closed after LastHttpContent has been consumed

       if (readTimedOut) {
          responseDone.completeExceptionally(new TimeoutException("Stream closed: read timeout"));
       } else if (jerseyRequest.isCancelled()) {
          responseDone.completeExceptionally(new CancellationException());
       } else {
          responseDone.completeExceptionally(new IOException("Stream closed"));
       }
    }

    protected void notifyResponse(ChannelHandlerContext ctx) {
       if (jerseyResponse != null) {
          ClientResponse cr = jerseyResponse;
          jerseyResponse = null;
          int responseStatus = cr.getStatus();
          if (Boolean.TRUE.equals(requestConfiguration.followRedirects())
                  && (responseStatus == ResponseStatus.Redirect3xx.MOVED_PERMANENTLY_301.getStatusCode()
                          || responseStatus == ResponseStatus.Redirect3xx.FOUND_302.getStatusCode()
                          || responseStatus == ResponseStatus.Redirect3xx.SEE_OTHER_303.getStatusCode()
                          || responseStatus == ResponseStatus.Redirect3xx.TEMPORARY_REDIRECT_307.getStatusCode()
                          || responseStatus == ResponseStatus.Redirect3xx.PERMANENT_REDIRECT_308.getStatusCode())) {
              String location = cr.getHeaderString(HttpHeaders.LOCATION);
              if (location == null || location.isEmpty()) {
                  responseAvailable.completeExceptionally(new RedirectException(LocalizationMessages.REDIRECT_NO_LOCATION()));
              } else {
                  try {
                      URI newUri = URI.create(location);
                      if (!newUri.isAbsolute()) {
                          final URI originalUri = jerseyRequest.getUri();
                          newUri = new JerseyUriBuilder()
                                  .scheme(originalUri.getScheme())
                                  .userInfo(originalUri.getUserInfo())
                                  .host(originalUri.getHost())
                                  .port(originalUri.getPort())
                                  .uri(location)
                                  .build();
                      }
                      boolean alreadyRequested = !redirectUriHistory.add(newUri);
                      if (alreadyRequested) {
                          // infinite loop detection
                          responseAvailable.completeExceptionally(
                                  new RedirectException(LocalizationMessages.REDIRECT_INFINITE_LOOP()));
                      } else if (redirectUriHistory.size() > requestConfiguration.maxRedirects.get()) {
                          // maximal number of redirection
                          responseAvailable.completeExceptionally(new RedirectException(
                                  LocalizationMessages.REDIRECT_LIMIT_REACHED(requestConfiguration.maxRedirects.get())));
                      } else {
                          ClientRequest newReq = new ClientRequest(jerseyRequest);
                          newReq.setUri(newUri);
                          ctx.close();
                          if (redirectController.prepareRedirect(newReq, cr)) {
                              final NettyConnector newConnector =
                                      new NettyConnector(newReq.getClient(), connector.clientConfiguration);
                              newConnector.execute(newReq, redirectUriHistory, new CompletableFuture<ClientResponse>() {
                                  @Override
                                  public boolean complete(ClientResponse value) {
                                      newConnector.close();
                                      return responseAvailable.complete(value);
                                  }

                                  @Override
                                  public boolean completeExceptionally(Throwable ex) {
                                      newConnector.close();
                                      return responseAvailable.completeExceptionally(ex);
                                  }
                              });
                          } else {
                              responseAvailable.complete(cr);
                          }
                      }
                  } catch (IllegalArgumentException e) {
                      responseAvailable.completeExceptionally(
                              new RedirectException(LocalizationMessages.REDIRECT_ERROR_DETERMINING_LOCATION(location)));
                  }
              }
          } else {
              responseAvailable.complete(cr);
          }
       }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (jerseyRequest.isCancelled()) {
            responseAvailable.completeExceptionally(new CancellationException());
            return;
        }
        if (msg instanceof HttpResponse) {
            final HttpResponse response = (HttpResponse) msg;
            jerseyResponse = new ClientResponse(new Response.StatusType() {
                @Override
                public int getStatusCode() {
                    return response.status().code();
                }

                @Override
                public Response.Status.Family getFamily() {
                    return Response.Status.Family.familyOf(response.status().code());
                }

                @Override
                public String getReasonPhrase() {
                    return response.status().reasonPhrase();
                }
            }, jerseyRequest);

            for (Map.Entry<String, String> entry : response.headers().entries()) {
                jerseyResponse.getHeaders().add(entry.getKey(), entry.getValue());
            }

            // request entity handling.
            nis = new NettyInputStream();
            responseDone.whenComplete((_r, th) -> nis.complete(th));

            jerseyResponse.setEntityStream(nis);
        }
        if (msg instanceof HttpContent) {

            HttpContent httpContent = (HttpContent) msg;

            ByteBuf content = httpContent.content();

            if (content.isReadable()) {
                content.retain();
                if (nis == null) {
                    nis = new NettyInputStream();
                }
                nis.publish(content);
            }

            if (msg instanceof LastHttpContent) {
                responseDone.complete(null);
                notifyResponse(ctx);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause) {
        responseDone.completeExceptionally(cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
       if (evt instanceof IdleStateEvent) {
          readTimedOut = true;
          ctx.close();
       } else {
           super.userEventTriggered(ctx, evt);
       }
    }

    /* package */ static class ProxyHeaders implements Predicate<String> {
        static final ProxyHeaders INSTANCE = new ProxyHeaders();
        private static final String HOST = HttpHeaders.HOST.toLowerCase(Locale.ROOT);
        private static final String FORWARDED = HttpHeaders.FORWARDED.toLowerCase(Locale.ROOT);

        @Override
        public boolean test(String headerName) {
            String lowName = headerName.toLowerCase(Locale.ROOT);
            return lowName.startsWith("proxy-") || lowName.equals(HOST) || lowName.equals(FORWARDED);
        }
    }
}
