package io.reactive.common.util;

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

import io.reactive.common.domain.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

public abstract class Actor {
    private static final Logger log = LoggerFactory.getLogger(Actor.class);

    private final Queue<Request> queue = new ConcurrentLinkedQueue<>();

    private final ExecutorService executorService;

    public Actor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected abstract void handleRequest(Request request);

    public void enqueue(Request r) {
        if (queue.offer(r)) {
            executorService.execute(new Task());
        } else {
            throw new IllegalStateException("No slots in the queue!");
        }
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            try {
                do {
                    Request r = queue.poll();
                    handleRequest(r);
                } while (!queue.isEmpty());
            } catch (Exception e) {
                log.error("Can't get request", e);
            }
        }
    }
}
