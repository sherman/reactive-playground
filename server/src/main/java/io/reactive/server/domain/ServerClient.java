package io.reactive.server.domain;

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

import com.google.common.base.MoreObjects;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.collect.ImmutableList.copyOf;

public class ServerClient {
    private static final Logger log = LoggerFactory.getLogger(ServerClient.class);

    private final long id;
    private final CopyOnWriteArrayList<ServerClientConnection> connections = new CopyOnWriteArrayList<>();

    public ServerClient(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void addConnection(@NotNull ServerClientConnection connection) {
        connections.addIfAbsent(connection);
    }

    public void removeConnection(@NotNull ServerClientConnection connection) {
        connections.remove(connection);
    }

    public List<ServerClientConnection> getConnections() {
        return copyOf(connections);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerClient that = (ServerClient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", id)
            .toString();
    }
}
