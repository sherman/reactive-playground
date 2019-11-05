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
import io.reactive.server.endpoint.MessageGeneratorEndpoint;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.Options;

import javax.servlet.ServletException;

public class UndertowWsServer {
    private static final Logger log = LoggerFactory.getLogger(ReactiveWsServer.class);

    private final int SHUTDOWN_TIME = 5000;
    private volatile Undertow server;

    public UndertowWsServer() {
        log.info("Server instance created");
    }

    public void start() throws Exception {
        log.info("Starting reactive server");

        System.setProperty("org.jboss.logging.provider", "slf4j");

        PathHandler path = Handlers.path();

        server = Undertow.builder()
            .addHttpListener(6644, "localhost")
            .setHandler(path)
            .setWorkerOption(Options.WORKER_IO_THREADS, Runtime.getRuntime().availableProcessors() * 2)
            .setWorkerOption(Options.WORKER_TASK_CORE_THREADS, 8)
            .setWorkerOption(Options.WORKER_TASK_MAX_THREADS, 8)
            .setWorkerOption(Options.TCP_NODELAY, true)
            .setSocketOption(Options.TCP_NODELAY, true)
            .setSocketOption(Options.REUSE_ADDRESSES, true)
            .build();

        server.start();

        ServletContainer container = ServletContainer.Factory.newInstance();

        DeploymentInfo builder = new DeploymentInfo()
            .setClassLoader(UndertowWsServer.class.getClassLoader())
            .setContextPath("/")
            .setResourceManager(new ClassPathResourceManager(UndertowWsServer.class.getClassLoader(), UndertowWsServer.class.getPackage()))
            .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME,
                new WebSocketDeploymentInfo()
                    .setBuffers(new DefaultByteBufferPool(true, 8192))
                    .addEndpoint(ClientEndpoint.class)
                    .addEndpoint(IncomingMessageEndpoint.class)
                    .addEndpoint(MessageGeneratorEndpoint.class)
            )
            .setDeploymentName("Server");

        DeploymentManager manager = container.addDeployment(builder);
        manager.deploy();

        try {
            path.addPrefixPath("/", manager.start());
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() throws Exception {
        log.info("Stopping reactive server");
        server.stop();
    }
}
