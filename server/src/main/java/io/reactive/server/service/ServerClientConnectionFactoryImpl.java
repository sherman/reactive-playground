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

import io.reactive.server.configuration.ServerConfiguration;
import io.reactive.server.domain.ServerClientConnection;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.util.function.Consumer;

@Singleton
public class ServerClientConnectionFactoryImpl implements ServerClientConnectionFactory {
    @Inject
    protected ServerConfiguration serverConfiguration;

    protected static final EmptyCallback EMPTY_CALLBACK = new EmptyCallback();

    @Override
    public ServerClientConnection create(long userId, Session session) {
        return new ServerClientConnection(userId, session, serverConfiguration.getMaxMessagesInFlight(), EMPTY_CALLBACK);
    }

    private static final class EmptyCallback implements Consumer<SendResult> {
        @Override
        public void accept(SendResult ignored) {
        }
    }

}
