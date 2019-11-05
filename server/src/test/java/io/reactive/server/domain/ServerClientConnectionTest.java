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

import io.reactive.common.configuration.RootModule;
import io.reactive.server.configuration.ServerConfiguration;
import io.reactive.server.service.ServerClientConnectionFactory;
import io.reactive.server.util.WebSocketSubscription;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@Guice(modules = {RootModule.class})
public class ServerClientConnectionTest {
    @Inject
    private ServerClientConnectionFactory connectionFactory;

    @Inject
    private ServerConfiguration serverConfiguration;

    @Test
    public void initialDemand() {
        WebSocketSubscription socketSubscription = new WebSocketSubscription();
        Session session = mock(Session.class);
        ServerClientConnection clientConnection = connectionFactory.create(42L, session);
        clientConnection.onSubscribe(socketSubscription);
        assertEquals(socketSubscription.getDemand().current(), serverConfiguration.getMaxMessagesInFlight());
    }

    @Test
    public void demandIsIncreased() {
        Session session = mock(Session.class);
        RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
        when(session.getAsyncRemote()).thenReturn(async);
        doAnswer((Answer<Void>) invocation -> {
            SendHandler callback = invocation.getArgument(1, SendHandler.class);
            callback.onResult(new SendResult());
            return null;
        }).when(async)
            .sendText(anyString(), any(SendHandler.class));

        WebSocketSubscription socketSubscription = new WebSocketSubscription();
        ServerClientConnection clientConnection = connectionFactory.create(42L, session);
        clientConnection.onSubscribe(socketSubscription);
        clientConnection.onNext(new WebSocketMessage("{}", System.currentTimeMillis()));
        assertEquals(socketSubscription.getDemand().current(), serverConfiguration.getMaxMessagesInFlight() + 1);
    }
}
