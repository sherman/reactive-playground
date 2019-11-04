package io.reactive.server.util;

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

import com.google.common.collect.ForwardingQueue;
import io.reactive.server.domain.WebSocketMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Represents a bound message queue for a single client.
 */
public class ServerClientMessageList implements Iterable<WebSocketMessage> {
    private final ForwardingQueue<WebSocketMessage> messages;

    public ServerClientMessageList(ForwardingQueue<WebSocketMessage> messages) {
        this.messages = messages;
    }

    public void add(@NotNull WebSocketMessage message) {
        // TODO: use non blocking queue (on CAS)
        synchronized (messages) {
            messages.offer(message);
        }
    }

    public void add(@NotNull List<WebSocketMessage> messages) {
        synchronized (this.messages) {
            this.messages.addAll(messages);
        }
    }

    public int getSize() {
        synchronized (messages) {
            return messages.size();
        }
    }

    @NotNull
    @Override
    public Iterator<WebSocketMessage> iterator() {
        return new ServerClientMessageListIterator();
    }

    private class ServerClientMessageListIterator implements Iterator<WebSocketMessage> {
        @Override
        public boolean hasNext() {
            synchronized (messages) {
                return !messages.isEmpty();
            }
        }

        @Override
        public WebSocketMessage next() {
            synchronized (messages) {
                if (messages.isEmpty()) {
                    throw new NoSuchElementException();
                }

                return messages.poll();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("ServerClientMessageListIterator is not modifiable!");
        }
    }
}
