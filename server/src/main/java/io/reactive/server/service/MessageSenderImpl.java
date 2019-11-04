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

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import io.reactive.server.configuration.ActorScope;
import io.reactive.server.configuration.ServerConfiguration;
import io.reactive.server.domain.Message;
import io.reactive.server.domain.ServerClient;
import io.reactive.server.domain.ServerClientConnection;
import io.reactive.server.domain.WebSocketMessage;
import io.reactive.server.util.Actor;
import io.reactive.server.util.ServerClientMessageList;
import io.reactive.server.util.WebSocketSubscription;
import io.reactive.server.util.WebSocketUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

@Singleton
public class MessageSenderImpl extends Actor<MessageSenderImpl.BaseMessage> implements MessageSender {
    private static final Logger log = LoggerFactory.getLogger(MessageSenderImpl.class);

    private final ConcurrentMap<Long, ServerClientMessageList> clientsToMessages = new ConcurrentHashMap<>();

    private final ServerClientStore serverClientStore;
    private final WebSocketUtils webSocketUtils;
    private final ServerConfiguration serverConfiguration;

    @Inject
    public MessageSenderImpl(
        @ActorScope ExecutorService actorExecutor,
        ServerClientStore serverClientStore,
        WebSocketUtils webSocketUtils,
        ServerConfiguration serverConfiguration
    ) {
        super(actorExecutor);
        this.serverClientStore = serverClientStore;
        this.webSocketUtils = webSocketUtils;
        this.serverConfiguration = serverConfiguration;
    }

    @Override
    public void addMessage(long clientId, @NotNull Message message) {
        ServerClientMessageList messages = clientsToMessages.computeIfAbsent(
            clientId, client -> new ServerClientMessageList(EvictingQueue.create(serverConfiguration.getMaxMessages())));

        messages.add(webSocketUtils.getMessage(message));
        enqueue(new Send(clientId));
    }

    @Override
    public void addMessages(long clientId, @NotNull List<Message> messages) {
        ServerClientMessageList userMessages = clientsToMessages.computeIfAbsent(
            clientId, client -> new ServerClientMessageList(EvictingQueue.create(serverConfiguration.getMaxMessages())));

        for (Message message : messages) {
            userMessages.add(webSocketUtils.getMessage(message));
        }

        enqueue(new Send(clientId));
    }

    @Override
    protected void dispatch(BaseMessage message) {
        log.trace("Handle message [{}]", message);

        if (message instanceof Send) {
            onSend((Send) message);
        }
    }

    private void onSend(Send message) {
        try {
            ServerClientMessageList messages = clientsToMessages.get(message.clientId);
            ServerClient client = serverClientStore.getClient(message.clientId);

            if (client != null && messages != null) {
                //log.info("Handle onSend message for client [{}]", client);

                for (ServerClientConnection clientConnection : client.getConnections()) {
                    WebSocketSubscription subscription = clientConnection.getSubscription();
                    Preconditions.checkArgument(subscription != null, "Subscription is required!");

                    if (!subscription.getDemand().isFulfilled()) {
                        Iterator<WebSocketMessage> iterator = messages.iterator();
                        while (iterator.hasNext()) {
                            if (subscription.getDemand().decrease(1) > 0) {
                                clientConnection.onNext(iterator.next());
                            } else {
                                clientConnection.onDemandIsFullFilled();
                                break;
                            }
                        }
                    }

                    if (messages.getSize() > 0) {
                        if (log.isTraceEnabled()) {
                            log.trace(
                                "Message left: [{}], discarded: [{}], is full filled: [{}]",
                                messages.getSize(),
                                clientConnection.getDemandIsFullFilledEvents(),
                                subscription.getDemand().isFulfilled()
                            );
                        }
                        enqueue(new Send(message.clientId));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error happens, while sending a message", e);
            terminate();
        }
    }

    private void terminate() {
        log.error("Can't continue sending messages");
        stop();
    }

    abstract static class BaseMessage {
    }

    private static class Send extends BaseMessage {
        private final long clientId;

        private Send(long clientId) {
            this.clientId = clientId;
        }
    }
}
