package io.reactive.client.service;

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

import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

@WebSocket
public class WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(WebSocketHandler.class);

    private final AtomicInteger messageCounter;

    public WebSocketHandler(AtomicInteger messageCounter) {
        this.messageCounter = messageCounter;
    }

    @OnWebSocketMessage
    public void onTextMessage(String message) {
        int messages = messageCounter.incrementAndGet();
        if (messages % 100000 == 0) {
            log.info("Messages: {}", messageCounter.get());
        }
    }
}
