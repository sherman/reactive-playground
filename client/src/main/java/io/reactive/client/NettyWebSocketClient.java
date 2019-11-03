package io.reactive.client;

/*
 * Copyright (C) 2019 by Denis M. Gabaydulin
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyWebSocketClient {
    private final URI url;
    private final EventLoopGroup group = new NioEventLoopGroup();
    private volatile Channel channel;
    private final AtomicInteger messageCounter;

    public NettyWebSocketClient(URI url, AtomicInteger messageCounter) {
        this.url = url;
        this.messageCounter = messageCounter;
    }

    public void connect() throws InterruptedException, IOException {
        WebSocketClientHandler handler =
            new WebSocketClientHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                    url, WebSocketVersion.V13, null, false, new DefaultHttpHeaders()), messageCounter);

        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(
                        new HttpClientCodec(),
                        new HttpObjectAggregator(8192),
                        handler
                    );
                }
            });

        this.channel = b.connect(url.getHost(), url.getPort()).sync().channel();
        handler.handshakeFuture().sync();
    }

    public void sendText(String message) {
        WebSocketFrame frame = new TextWebSocketFrame(message);
        channel.writeAndFlush(frame);
    }

    public void close() {
        channel.close();
        group.shutdownGracefully();
    }
}
