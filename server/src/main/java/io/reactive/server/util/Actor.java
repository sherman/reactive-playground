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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Actor<M> implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Actor.class);

    private final Queue<M> messages = new ConcurrentLinkedQueue<>();
    private final ExecutorService actorExecutor;
    private final AtomicBoolean working = new AtomicBoolean(false);
    private volatile boolean stop = false;

    public Actor(ExecutorService actorExecutor) {
        this.actorExecutor = actorExecutor;
    }

    protected abstract void dispatch(M message);

    @Override
    public void run() {
        if (working.get()) {
            try {
                process();
            } catch (Exception e) {
                log.error("Can't process message", e);
            } finally {
                working.set(false);
                if (!messages.isEmpty()) {
                    tryToScheduleExecution();
                }
            }
        }
    }

    protected void process() {
        M message;
        while ((message = dequeue()) != null && !isStopped()) {
            dispatch(message);
        }
    }

    protected final M dequeue() {
        return messages.poll();
    }

    private boolean isStopped() {
        return stop;
    }

    protected final void enqueue(M message) {
        if (messages.offer(message)) {
            tryToScheduleExecution();
        } else {
            // TODO: handle error?
        }
    }

    protected void stop() {
        stop = true;
        AtomicInteger discarded = new AtomicInteger();
        while (dequeue() != null) {
            discarded.incrementAndGet();
        }

        log.error("[{}] message were discarded", discarded.get());
    }

    private boolean tryToScheduleExecution() {
        if (working.compareAndSet(false, true)) {
            log.trace("Schedule execution");

            try {
                actorExecutor.execute(this);
                return true;
            } catch (Exception e) {
                log.error("Can't execute the actor", e);
                working.set(false);
                // TODO: process messages already in the queue?
                return false;
            }
        } else {
            //log.info("Already scheduled!");
        }

        return false;
    }
}
