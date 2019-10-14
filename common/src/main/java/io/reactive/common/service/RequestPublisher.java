package io.reactive.common.service;

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

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.reactive.common.domain.Request;
import io.reactive.common.domain.RequestSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.*;

@Singleton
public class RequestPublisher extends AbstractService implements Flow.Publisher<Request> {
    private static final Logger log = LoggerFactory.getLogger(RequestPublisher.class);

    private final ConcurrentHashMap<Long, RequestSubscription> subscriptions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Flow.Subscriber<? super Request>> subscribers = new ConcurrentHashMap<>();

    @Inject
    private ExecutorService actorExecutor;

    @Inject
    private RequestHandlerService requestHandlerService;

    private volatile ScheduledFuture<?> scheduledFuture;

    private final ScheduledExecutorService publisher = Executors.newScheduledThreadPool(
        1,
        new ThreadFactoryBuilder()
            .setDaemon(true)
            .setNameFormat("Publisher executor-%d")
            .build()
    );

    @Override
    public void subscribe(Flow.Subscriber<? super Request> subscriber) {
        Preconditions.checkArgument(subscriber instanceof RequestSubscriber, "RequestSubscriber is expected");

        RequestSubscriber identifiedSubscriber = (RequestSubscriber) subscriber;

        if (subscriptions.containsKey(identifiedSubscriber.getId())) {
            subscriber.onError(new IllegalArgumentException("Already subscribed!"));
        }

        // FIXME: remove race
        subscriptions.put(identifiedSubscriber.getId(), new RequestSubscription(actorExecutor, requestHandlerService, identifiedSubscriber.getId()));
        subscribers.put(identifiedSubscriber.getId(), subscriber);

        identifiedSubscriber.onSubscribe(subscriptions.get(identifiedSubscriber.getId()));
    }

    @Override
    protected void doStart() {
        scheduledFuture = publisher.scheduleWithFixedDelay(
            () -> {
                try {
                    subscribers.forEach(
                        (k, v) -> {
                            if (subscriptions.get(k).getCurrent() > 0) {
                                v.onNext(new Request());
                            }
                        }
                    );
                } catch (Exception e) {
                    log.error("Can't publish a message", e);
                }
            },
            100,
            100,
            TimeUnit.MILLISECONDS
        );

        notifyStarted();
    }

    @Override
    protected void doStop() {
        try {
            scheduledFuture.cancel(true);
        } finally {
            publisher.shutdown();
            notifyStopped();
        }
    }
}
