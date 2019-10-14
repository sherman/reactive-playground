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

import com.google.inject.assistedinject.Assisted;
import io.reactive.common.domain.Request;
import io.reactive.common.domain.RequestSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Flow;

public class RequestSubscriber implements Flow.Subscriber<Request> {
    private static final Logger log = LoggerFactory.getLogger(RequestSubscriber.class);

    private volatile RequestSubscription subscription;

    private final long id;
    private final RequestHandlerService requestHandlerService;

    @Inject
    public RequestSubscriber(RequestHandlerService requestHandlerService, @Assisted long id) {
        this.requestHandlerService = requestHandlerService;
        this.id = id;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = (RequestSubscription) subscription;
        this.subscription.request(1);
    }

    @Override
    public void onNext(Request request) {
        requestHandlerService.handle(request);
        this.subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Error in subscriber {}", id, throwable);
    }

    @Override
    public void onComplete() {
        log.info("Completed");
    }

    public long getId() {
        return id;
    }
}
