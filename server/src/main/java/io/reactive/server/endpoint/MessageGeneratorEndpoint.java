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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ServerEndpoint(value = "/generator", configurator = WsEndpointConfigurator.class)
public class MessageGeneratorEndpoint {
    private static final Logger log = LoggerFactory.getLogger(IncomingMessageEndpoint.class);

    @Inject
    private ServerClientStore clientStore;

    @Inject
    private MessageSender messageSender;

    @Inject
    private JsonMapper objectMapper;

    @Inject
    private ServerConfiguration serverConfiguration;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(
        1,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Generator-%d")
            .build()
    );

    private volatile ScheduledFuture<?> generator;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
    }

    @OnClose
    public void onClose(Session session, javax.websocket.CloseReason closeReason) {
        if (generator != null) {
            generator.cancel(true);
        }
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

        generator = executorService.scheduleWithFixedDelay(
            () -> {
                try {
                    if (serverConfiguration.isGeneratorWithBatches()) {
                        int buckets = serverConfiguration.getGeneratorMessages() / serverConfiguration.getMaxMessages();
                        for (int i = 0; i < buckets; i++) {
                            List<Message> messages = new ArrayList<>(serverConfiguration.getMaxMessages());
                            for (int j = 0; j < serverConfiguration.getMaxMessages(); j++) {
                                messages.add(userMessage);
                            }

                            clientStore.getClients().forEach(
                                client -> client.getConnections().forEach(
                                    connection -> {
                                        try {
                                            messageSender.addMessages(client.getId(), messages);
                                        } catch (Exception e) {
                                            log.error("Can't send a message", e);
                                        }
                                    }
                                )
                            );
                        }
                    } else {
                        for (int i = 0; i < serverConfiguration.getGeneratorMessages(); i++) {
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
                } catch (Exception e) {
                    log.error("Can't add messages", e);
                }
            },
            0,
            serverConfiguration.getGeneratorMessagesPeriod(),
            TimeUnit.SECONDS
        );
    }
}
