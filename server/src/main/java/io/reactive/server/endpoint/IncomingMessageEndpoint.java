package io.reactive.server.endpoint;

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
import io.reactive.server.domain.Message;
import io.reactive.server.service.MessageSender;
import io.reactive.server.service.ServerClientStore;
import io.reactive.server.util.WsEndpointConfigurator;
import io.reactive.server.util.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/message", configurator = WsEndpointConfigurator.class)
public class IncomingMessageEndpoint {
    private static final Logger log = LoggerFactory.getLogger(IncomingMessageEndpoint.class);

    @Inject
    private ServerClientStore clientStore;

    @Inject
    private MessageSender messageSender;

    @Inject
    private JsonMapper objectMapper;

    @Inject
    private ServerConfiguration serverConfiguration;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
    }

    @OnClose
    public void onClose(Session session, javax.websocket.CloseReason closeReason) {
    }

    @OnError
    public void onError(Session session, Throwable thr) {
    }

    @OnMessage
    public void onMessage(String message) {
        Message userMessage;
        try {
            userMessage = objectMapper.readValue(message, Message.class);
        } catch (IOException e) {
            log.error("Invalid message: [{}]", message, e);
            throw new IllegalArgumentException("Invalid message!");
        }

        for (int i = 0; i < serverConfiguration.getMessageMultiplier(); i++) {
            clientStore.getClients().forEach(
                client -> client.getConnections().forEach(
                    connection -> {
                        try {
                            messageSender.addMessage(client.getId(), userMessage);
                        } catch (Exception e) {
                            log.error("Can't send a message", e);
                        }
                    }
                )
            );
        }
    }
}
