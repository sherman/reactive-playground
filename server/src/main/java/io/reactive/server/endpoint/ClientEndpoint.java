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

import io.reactive.server.domain.ServerClientConnection;
import io.reactive.server.service.ServerClientConnectionFactory;
import io.reactive.server.service.ServerClientStore;
import io.reactive.server.util.WebSocketUtils;
import io.reactive.server.util.WsEndpointConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.websocket.CloseReason.CloseCodes.CLOSED_ABNORMALLY;
import static javax.websocket.CloseReason.CloseCodes.VIOLATED_POLICY;

@ServerEndpoint(value = "/client", configurator = WsEndpointConfigurator.class)
public class ClientEndpoint {
    private static final Logger log = LoggerFactory.getLogger(ClientEndpoint.class);

    @Inject
    private WebSocketUtils webSocketUtils;

    @Inject
    private ServerClientStore clientStore;

    @Inject
    private ServerClientConnectionFactory connectionFactory;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        ServerClientConnection clientConnection = null;

        try {
            Long userId = webSocketUtils.getUserId(session.getQueryString());

            log.info("Open session for user [{}]", session.getQueryString());

            if (userId == null) {
                log.error("Can't find user id");
                closeViolated(session);
                return;
            }

            log.info("Connection for user [{}]", userId);

            clientConnection = connectionFactory.create(userId, session);
            clientConnection.setAuthenticated(true); // TODO: make real authentication

            session.setMaxIdleTimeout(300000); // make configurable
            session.getAsyncRemote().setSendTimeout(10000);

            clientStore.addConnection(clientConnection);
        } catch (Exception e) {
            log.error("Can't open the connection", e);

            if (clientConnection != null && clientConnection.getSession().isOpen()) {
                close(session);
            }

            throw new RuntimeException(e);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        try {
            Long userId = webSocketUtils.getUserId(session.getQueryString());

            log.info("Close session [{}] for user [{}]", closeReason, session.getQueryString());

            checkNotNull(userId, "User id is required!");

            ServerClientConnection clientConnection = connectionFactory.create(userId, session);

            clientStore.removeConnection(clientConnection);
        } catch (Exception e) {
            log.error("Can't close the connection", e);

            throw new RuntimeException(e);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        try {
            Long userId = webSocketUtils.getUserId(session.getQueryString());

            log.error("Exception occurred for user [{}]", session.getQueryString(), thr);

            checkNotNull(userId, "User id is required!");

            ServerClientConnection clientConnection = connectionFactory.create(userId, session);

            clientStore.removeConnection(clientConnection);
        } catch (Exception e) {
            log.error("Can't remove connection for user [{}]", session.getQueryString(), e);
        }
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("Message [{}] were discarded", message);
    }

    private void closeViolated(Session session) {
        try {
            session.close(new CloseReason(VIOLATED_POLICY, "Invalid request"));
        } catch (IOException e) {
            log.error("Can't close the connection", e);
        }
    }

    private void close(Session session) {
        try {
            session.close(new CloseReason(CLOSED_ABNORMALLY, "Strange thing's happen"));
        } catch (IOException e) {
            log.error("Can't close the connection", e);
        }
    }
}
