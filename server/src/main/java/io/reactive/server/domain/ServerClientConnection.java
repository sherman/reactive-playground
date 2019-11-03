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
import io.reactive.server.util.WebSocketSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.SendResult;
import javax.websocket.Session;
import java.util.Objects;
import java.util.concurrent.Flow;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ServerClientConnection implements Flow.Subscriber<WebSocketMessage> {
    private static final Logger log = LoggerFactory.getLogger(ServerClientConnection.class);

    private final long userId;
    private final Session session;
    private final int maxMessagesInFlight;
    private final Consumer<SendResult> resultHandler;
    private volatile boolean authenticated;

    private WebSocketSubscription subscription;

    private final AtomicInteger demandIsFullFilledEvents = new AtomicInteger();
    private final AtomicInteger sentMessages = new AtomicInteger();

    public ServerClientConnection(long userId, Session session, int maxMessagesInFlight, Consumer<SendResult> resultHandler) {
        this.userId = userId;
        this.session = session;
        this.maxMessagesInFlight = maxMessagesInFlight;
        this.resultHandler = resultHandler;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public long getUserId() {
        return userId;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerClientConnection that = (ServerClientConnection) o;
        return Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("userId", userId)
            .add("session", session)
            .add("authenticated", authenticated)
            .toString();
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = (WebSocketSubscription) subscription;

        doRequest(maxMessagesInFlight);
    }

    @Override
    public void onNext(WebSocketMessage item) {
        try {
            session.getAsyncRemote().sendText(item.getPayload(), this::onResult);
        } catch (Exception e) {
            log.error("Can't send the message to client", e);
            // TODO: close connection?
        } finally {
            onSendMessage();
        }
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
    }

    public WebSocketSubscription getSubscription() {
        return subscription;
    }

    public void onDemandIsFullFilled() {
        demandIsFullFilledEvents.incrementAndGet();
    }

    public int getDemandIsFullFilledEvents() {
        return demandIsFullFilledEvents.get();
    }

    public void onSendMessage() {
        sentMessages.incrementAndGet();
    }

    public int getSentMessages() {
        return sentMessages.get();
    }

    private void doRequest(int messages) {
        subscription.request(messages);
    }

    private void onResult(SendResult result) {
        if (result.getException() != null) {
            log.error("Can' send the message", result.getException());
        }

        resultHandler.accept(result);

        // increase demand
        doRequest(1);
    }
}
