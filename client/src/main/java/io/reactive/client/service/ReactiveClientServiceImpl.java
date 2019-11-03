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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactive.client.domain.ClientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ReactiveClientServiceImpl implements ReactiveClientService {
    private static final Logger log = LoggerFactory.getLogger(ReactiveClientServiceImpl.class);

    private final ExecutorService executorService = Executors.newFixedThreadPool(
        8,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Client executor-%d")
            .build()
    );

    private final ReactiveClientStore reactiveClientStore;

    public ReactiveClientServiceImpl(String serverUrlTemplate, ClientType clientType) {
        switch (clientType) {
            case JETTY:
                this.reactiveClientStore = new JettyReactiveClientStoreImpl(serverUrlTemplate);
                break;

            case NETTY:
                this.reactiveClientStore = new NettyReactiveClientStoreImpl(serverUrlTemplate);
                break;

            default:
                throw new IllegalArgumentException("Unknown client!");
        }
    }

    @Override
    public void open(int clients) {
        final CountDownLatch latch = new CountDownLatch(clients);

        for (int i = 0; i < clients; i++) {
            final int id = i + 1;
            executorService.submit(
                (Runnable) () -> {
                    try {
                        reactiveClientStore.addClient(id);
                    } catch (Exception e) {
                        log.error("Can't open client", e);
                    } finally {
                        latch.countDown();
                    }
                }
            );
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("interrupted", e);
        }

        log.info("Clients are connected");
    }

    @Override
    public void waitAndClose(final int totalMessages) {
        final AtomicInteger totalMessagesCounter = new AtomicInteger(totalMessages);
        final Semaphore semaphore = new Semaphore(0, true);

        executorService.submit(
            (Runnable) () -> {
                while (true) {
                    try {
                        int current = reactiveClientStore.getMessageCount();

                        if (current >= totalMessages) {
                            semaphore.release();
                            return;
                        }

                        tryDecrement(totalMessagesCounter, totalMessages - current);
                    } catch (Exception e) {
                        log.error("Can't get message count", e);
                    }

                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                    }
                }
            }
        );

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
        }

        log.info("All messages received!");

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        reactiveClientStore.removeAll();

        executorService.shutdownNow();
    }

    private static void tryDecrement(AtomicInteger messageCounter, int newState) {
        while (true) {
            int state = messageCounter.get();
            if (messageCounter.compareAndSet(state, newState)) {
                return;
            }
        }
    }
}
