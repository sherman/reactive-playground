package io.reactive.server.service;

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

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Striped;
import io.reactive.server.domain.ServerClient;
import io.reactive.server.domain.ServerClientConnection;
import io.reactive.server.util.WebSocketSubscription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;

import static com.google.common.util.concurrent.Striped.lock;
import static org.slf4j.LoggerFactory.getLogger;

@Singleton
public class ServerClientStoreImpl implements ServerClientStore {
    private static final Logger log = getLogger(ServerClientStoreImpl.class);

    private final ConcurrentMap<Long, ServerClient> clients = new ConcurrentHashMap<>();

    private final Striped<Lock> locksPool = lock(256);

    @Override
    public ServerClient atomicAddClient(@NotNull ServerClient client) {
        log.debug("Add user: [{}]", client);

        ServerClient added = clients.computeIfAbsent(client.getId(), clientId -> client);

        if (added == client) {
            log.debug("New user");
        }

        return added;
    }

    @Override
    public void addConnection(@NotNull ServerClientConnection connection) {
        log.debug("Add connection for user [{}]", connection.getUserId());

        Lock lock = locksPool.get(connection.getUserId());
        lock.lock();

        try {
            log.info("Subscription for user [{}]", connection.getUserId());
            WebSocketSubscription webSocketSubscription = new WebSocketSubscription();
            connection.onSubscribe(webSocketSubscription);
            log.info("Subscribed successfully for user [{}]", connection.getUserId());

            ServerClient actual = atomicAddClient(new ServerClient(connection.getUserId()));
            actual.addConnection(connection);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeConnection(@NotNull ServerClientConnection connection) {
        log.debug("Remove connection for user [{}]", connection.getUserId());

        ServerClient actual = getClient(connection.getUserId());

        if (null != actual) {
            Lock lock = locksPool.get(connection.getUserId());
            lock.lock();

            try {
                // TODO: move?
                connection.onComplete();

                actual.removeConnection(connection);

                int connections = actual.getConnections().size();

                log.debug("User [{}] has {} connections", actual, connections);

                if (connections > 0) {
                    log.debug("User connections greater than 0: [{}]", connections);
                    return;
                }

                removeClient(connection.getUserId());
            } finally {
                lock.unlock();
            }
        }
    }

    @Nullable
    @Override
    public ServerClient getClient(long id) {
        return clients.get(id);
    }

    @Override
    public boolean hasClient(long id) {
        return clients.containsKey(id);
    }

    @Override
    public List<ServerClient> getClients() {
        return ImmutableList.copyOf(clients.values());
    }

    @Override
    public void clear() {
        clients.clear();
    }

    private boolean removeClient(long id) {
        log.debug("Remove user: [{}]", id);

        return clients.remove(id) != null;
    }
}
