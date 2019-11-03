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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactive.server.configuration.RootModule;
import io.reactive.server.service.ServerClientConnectionFactory;
import io.reactive.server.util.Demand;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Guice;

import javax.inject.Inject;
import javax.websocket.RemoteEndpoint;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import javax.websocket.Session;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.*;

@Guice(modules = {RootModule.class})
public class ServerClientTest {
    private static final Logger log = LoggerFactory.getLogger(ServerClientTest.class);

    @Inject
    private ServerClientConnectionFactory connectionFactory;

    private final ScheduledExecutorService publisher = Executors.newScheduledThreadPool(
        1,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Publisher executor-%d")
            .build()
    );

    //@Test
    public void backPressure() throws InterruptedException {
        AtomicInteger messages = new AtomicInteger();
        Session session = mock(Session.class);
        RemoteEndpoint.Async async = mock(RemoteEndpoint.Async.class);
        when(session.getAsyncRemote()).thenReturn(async);
        doAnswer((Answer<Void>) invocation -> {
            //Thread.sleep(100);
            messages.incrementAndGet();
            SendHandler callback = invocation.getArgument(1, SendHandler.class);
            callback.onResult(new SendResult());
            return null;
        }).when(async)
            .sendText(anyString(), any(SendHandler.class));
        ServerClientConnection clientConnection = connectionFactory.create(42L, session);

        ServerClient serverClient = new ServerClient(42L);
        serverClient.addConnection(clientConnection);

        WsClientSubscription wsClientSubscription = new WsClientSubscription(publisher, serverClient);
        //serverClient.onSubscribe(wsClientSubscription);

        while (messages.get() < 100000) {
            //Thread.sleep(2000);
            //log.info("Handled messages: [{}]", messages.get());
        }

        log.info("Handled messages: [{}]", messages.get());
    }

    @AfterClass
    private void close() {
        publisher.shutdown();
    }

    private static class WsClientSubscription implements Flow.Subscription {
        private final Demand demand = new Demand();

        private final ScheduledFuture<?> scheduledFuture;
        private final ServerClient serverClient;

        private WsClientSubscription(ScheduledExecutorService executorService, ServerClient serverClient) {
            this.serverClient = serverClient;
            scheduledFuture = executorService.scheduleWithFixedDelay(
                () -> {
                    try {
                        if (!demand.isFulfilled()) {
                            long items = demand.decrease(demand.current());
                            for (int i = 0; i < items; i++) {
                                //serverClient.onNext(new WebSocketMessage("42", System.currentTimeMillis()));
                            }
                        }
                    } catch (Exception e) {
                        log.error("Can't push message", e);

                    }
                },
                100,
                100,
                TimeUnit.MILLISECONDS
            );
        }

        @Override
        public void request(long n) {
            demand.increase(n);
        }

        @Override
        public void cancel() {
            demand.reset();
            scheduledFuture.cancel(true);
        }
    }
}
