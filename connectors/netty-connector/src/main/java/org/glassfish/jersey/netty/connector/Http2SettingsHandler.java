/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved.
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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2Settings;

public class Http2SettingsHandler extends SimpleChannelInboundHandler<Http2Settings> {
    private final ChannelPromise promise;

    /**
     * Create new instance
     *
     * @param promise Promise object used to notify when first settings are received
     */
    public Http2SettingsHandler(ChannelPromise promise) {
        this.promise = promise;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2Settings msg) throws Exception {
        promise.setSuccess();


        // Only care about the first settings message
        ctx.pipeline().remove(this);
    }
}