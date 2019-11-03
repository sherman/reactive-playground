package io.reactive.server.integration;

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

import com.google.common.base.Charsets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Injector;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.core.ConditionTimeoutException;
import io.reactive.server.ReactiveWsServer;
import io.reactive.server.config.CommonModule;
import io.reactive.server.configuration.RootModule;
import io.reactive.server.service.ServerClientStore;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketFrame;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.extensions.Frame;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.inject.Inject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertEquals;

@Guice(modules = {RootModule.class, CommonModule.class})
public class IntegrationTests {
    private static final Logger log = LoggerFactory.getLogger(IntegrationTests.class);

    @Inject
    private Injector injector;

    @Inject
    private ReactiveWsServer server;

    @Inject
    private WebSocketClient client;

    @Inject
    private ServerClientStore serverClientStore;

    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(
        1,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Helper executor-%d")
            .build()
    );

    private static final String HELLO_MESSAGE = "{\"header\":{\"messageId\":\"42\",\"messageType\":\"HELLO_MESSAGE\",\"recipient\":\"42\",\"created\":\"2018-01-14T13:27:52.178Z\"},\"message\":{\"hello\":\"World\"}}";

    @Test
    public void simpleSendMessage() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        WebSocketHandler clientMessagesHandler = new WebSocketHandler(latch);
        Session session = client.connect(clientMessagesHandler, new URI("ws://127.0.0.1:6644/client?userId=42"))
            .get();

        // send 2 messages
        Session messagesEndpointSession = client.connect(new WebSocketHandler(2), new URI("ws://127.0.0.1:6644/message"))
            .get();
        messagesEndpointSession.getRemote().sendString(HELLO_MESSAGE);
        messagesEndpointSession.getRemote().sendString(HELLO_MESSAGE);
        messagesEndpointSession.close();

        latch.await();

        assertEquals(clientMessagesHandler.getMessages().size(), 2);

        session.close();
    }

    @Test
    public void checkDemand() throws Exception {
        // it's required sending enough messages to make a congestion
        int messages = 25000;
        AtomicBoolean barrier = new AtomicBoolean(true);
        CountDownLatch receiver = new CountDownLatch(messages);
        SlowWebSocketHandler clientMessagesHandler = new SlowWebSocketHandler(receiver, barrier, 10L);
        Session session = client.connect(clientMessagesHandler, new URI("ws://127.0.0.1:6644/client?userId=42"))
            .get();

        Thread.sleep(100);

        // send messages
        Session messagesEndpointSession = client.connect(new WebSocketHandler(messages), new URI("ws://127.0.0.1:6644/message"))
            .get();
        for (int i = 0; i < messages; i++) {
            messagesEndpointSession.getRemote().sendString(HELLO_MESSAGE);
        }
        messagesEndpointSession.close();

        // wait for demand is full filled events
        Awaitility.await().forever().until(
            () -> serverClientStore.getClient(42L).getConnections().get(0).getDemandIsFullFilledEvents() > 0
        );

        log.info("Demand is full filled events: [{}]", serverClientStore.getClient(42L).getConnections().get(0).getDemandIsFullFilledEvents());
        log.info("Sent messages: [{}]", serverClientStore.getClient(42L).getConnections().get(0).getSentMessages());

        // drop off the barrier
        barrier.set(false);

        log.info("Drop barrier");

        receiver.await();

        log.info("Received all messages");

        assertEquals(clientMessagesHandler.getMessages().size(), messages);

        session.close();
    }

    @BeforeClass
    private void setUp() throws Exception {
        server.start();

        client = new WebSocketClient(
            Executors.newFixedThreadPool(8,
                new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("Client executor-%d")
                    .build()
            )
        );
        client.setConnectTimeout(5000);
        client.start();
    }

    @AfterClass
    private void tearDown() throws Exception {
        client.stop();
        server.stop();
        scheduledExecutorService.shutdown();
    }

    @BeforeTest
    private void reset() {
    }

    @WebSocket
    public static class WebSocketHandler {
        private final CountDownLatch latch;
        private final List<String> messages = new ArrayList<>();

        public WebSocketHandler(CountDownLatch latch) {
            this.latch = latch;
        }

        public WebSocketHandler(int messages) {
            this(new CountDownLatch(messages));
        }

        @OnWebSocketMessage
        public final void onTextMessage(String message) {
            handleMessage(message);
        }

        public List<String> getMessages() {
            return messages;
        }

        protected void handleMessage(String message) {
            log.info("Message: [{}]", message);
            messages.add(message);
            latch.countDown();
        }
    }

    @WebSocket
    public static class SlowWebSocketHandler {
        private final CountDownLatch latch;
        private final AtomicBoolean barrier;
        private final List<String> messages = new ArrayList<>();
        private final long sleepMills;

        public SlowWebSocketHandler(CountDownLatch latch, AtomicBoolean barrier, long sleepMills) {
            this.latch = latch;
            this.barrier = barrier;
            this.sleepMills = sleepMills;
        }

        @OnWebSocketFrame
        public final void onWebSocketFrame(Frame frame) throws InterruptedException {
            try {
                Awaitility.await()
                    .catchUncaughtExceptions()
                    .pollInterval(Math.min(sleepMills / 5, 10), TimeUnit.MILLISECONDS)
                    .atMost(sleepMills, TimeUnit.MILLISECONDS).until(barrier::get);
            } catch (ConditionTimeoutException ignored) {
            }

            byte[] buffer = new byte[frame.getPayload().remaining()];
            frame.getPayload().get(buffer);
            messages.add(new String(buffer, Charsets.UTF_8));
            if (messages.size() % 100 == 0 || messages.size() > 19000) {
                log.info("Received: {}", messages.size());
            }
            latch.countDown();
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
