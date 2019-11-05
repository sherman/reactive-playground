package io.reactive.client.service;

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

import io.reactive.client.ReactiveClient;
import io.reactive.client.configuration.ClientConfiguration;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: DI
public class JettyReactiveClientStoreImpl implements ReactiveClientStore {
    private static final Logger log = LoggerFactory.getLogger(JettyReactiveClientStoreImpl.class);

    private final String serverUrlTemplate; // http://127.0.0.1:6644/message?userId=

    private final ConcurrentMap<Long, ReactiveClient> clients = new ConcurrentHashMap<>();
    private final AtomicInteger messageCounter = new AtomicInteger();

    private final WebSocketClient client;
    private final WebSocketHandler webSocketHandler;

    public JettyReactiveClientStoreImpl(String serverUrlTemplate, ClientConfiguration clientConfiguration) {
        client = new WebSocketClient(Executors.newFixedThreadPool(64));
        client.setConnectTimeout(1000000);

        try {
            client.start();
        } catch (Exception e) {
            log.error("Can't start client", e);
            throw new RuntimeException(e);
        }

        this.serverUrlTemplate = serverUrlTemplate;
        this.webSocketHandler = new WebSocketHandler(messageCounter, clientConfiguration.getReadDelayMilliseconds());
    }

    @Override
    public void addClient(long userId) {
        Session session = null;
        try {
            session = client.connect(
                webSocketHandler,
                new URI(serverUrlTemplate + userId)
            ).get();
        } catch (Exception e) {
            log.error("Error", e);
        } finally {
            if (session != null) {
                clients.putIfAbsent(userId, new ReactiveClient(userId, session));
            }
        }
    }

    @Override
    public void removeAll() {
        for (ReactiveClient client : clients.values()) {
            client.getSession().close();
        }

        try {
            client.stop();
        } catch (Exception e) {
            log.error("Can't close client", e);
        }
    }

    @Override
    public int getMessageCount() {
        return messageCounter.get();
    }
}
