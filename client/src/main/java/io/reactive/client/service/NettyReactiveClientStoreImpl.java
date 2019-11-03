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

import io.reactive.client.NettyWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyReactiveClientStoreImpl implements ReactiveClientStore {
    private static final Logger log = LoggerFactory.getLogger(NettyReactiveClientStoreImpl.class);

    private final ConcurrentMap<Long, NettyWebSocketClient> clients = new ConcurrentHashMap<>();
    private final AtomicInteger messageCounter = new AtomicInteger();
    private final String serverUrlTemplate;

    public NettyReactiveClientStoreImpl(String serverUrlTemplate) {
        this.serverUrlTemplate = serverUrlTemplate;
    }

    @Override
    public void addClient(long userId) {
        try {
            NettyWebSocketClient client = new NettyWebSocketClient(new URI(serverUrlTemplate + userId), messageCounter);
            client.connect();
            clients.putIfAbsent(userId, client);
        } catch (Exception e) {
            log.error("Can't start client", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeAll() {
        for (NettyWebSocketClient client : clients.values()) {
            client.close();
        }
    }

    @Override
    public int getMessageCount() {
        return messageCounter.get();
    }
}
