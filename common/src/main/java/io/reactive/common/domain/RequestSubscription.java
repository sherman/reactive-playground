package io.reactive.common.domain;

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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.reactive.common.service.RequestHandlerService;
import io.reactive.common.util.Actor;
import io.reactive.common.util.Demand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

public class RequestSubscription extends Actor implements Flow.Subscription {
    private static final Logger log = LoggerFactory.getLogger(RequestSubscription.class);

    // TODO: move to request
    private final long id;
    private final Demand demand = new Demand();
    private final RequestHandlerService requestHandlerService;

    public RequestSubscription(ExecutorService executorService, RequestHandlerService requestHandlerService, long id) {
        super(executorService);
        this.requestHandlerService = requestHandlerService;
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public void request(long n) {
        log.info("Request {} items", n);
        demand.increase(n);

        enqueue(new Request());
    }

    @Override
    public void cancel() {
        demand.reset();
    }

    public long getCurrent() {
        return demand.current();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestSubscription that = (RequestSubscription) o;

        return Objects.equal(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("id", id)
            .toString();
    }

    @Override
    protected void handleRequest(Request request) {
        requestHandlerService.handle(request);
    }
}
