package io.reactive.server;

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

import io.reactive.server.endpoint.ClientEndpoint;
import io.reactive.server.endpoint.IncomingMessageEndpoint;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.websocket.server.ServerContainer;

@Singleton
public class ReactiveWsServer {
    private static final Logger log = LoggerFactory.getLogger(ReactiveWsServer.class);

    private final int SHUTDOWN_TIME = 5000;
    private volatile Server server;

    public ReactiveWsServer() {
        log.info("Server instance created");
    }

    public void start() throws Exception {
        log.info("Starting reactive server");

        server = new Server(new QueuedThreadPool(512));

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(6644);
        connector.setReuseAddress(true);
        connector.setStopTimeout(SHUTDOWN_TIME);
        server.setConnectors(new Connector[]{connector});

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServerContainer container = WebSocketServerContainerInitializer.configureContext(context);
        container.addEndpoint(ClientEndpoint.class);
        container.addEndpoint(IncomingMessageEndpoint.class);

        server.setStopAtShutdown(true);

        server.start();

    }

    public void stop() throws Exception {
        log.info("Stopping reactive server");
        server.stop();
    }
}
