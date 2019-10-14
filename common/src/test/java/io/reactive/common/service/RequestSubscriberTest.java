package io.reactive.common.service;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.reactive.common.config.CommonModule;
import org.mockito.Mockito;
import org.testng.annotations.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.mockito.Mockito.spy;
import static org.testng.Assert.assertEquals;

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
@Guice(modules = {CommonModule.class, RequestSubscriberTest.Module.class})
public class RequestSubscriberTest {
    @Inject
    private RequestSubscriberFactory requestSubscriberFactory;

    @Inject
    private RequestPublisher requestPublisher;

    @Inject
    private RequestHandlerService requestHandlerService;

    @Test
    public void createSubscriber() {
        RequestSubscriber subscriber = requestSubscriberFactory.create(1L);
        assertEquals(subscriber.getId(), 1L);
    }

    @Test
    public void processData() throws InterruptedException {
        RequestSubscriber subscriber = requestSubscriberFactory.create(1L);
        requestPublisher.subscribe(subscriber);

        while (requestHandlerService.getHandled() < 10) {
            Thread.sleep(100);
        }
    }

    @BeforeClass
    private void start() {
        requestPublisher.startAsync();
        requestPublisher.awaitRunning();
    }

    @AfterClass
    private void stop() {
        requestPublisher.stopAsync();
        requestPublisher.awaitTerminated();
    }

    @BeforeTest
    private void reset() {
        Mockito.reset(requestHandlerService);
    }

    public static class Module extends AbstractModule {
        @Provides
        @Singleton
        RequestHandlerService requestHandlerService() {
            RequestHandlerServiceImpl service = new RequestHandlerServiceImpl();
            return spy(service);
        }
    }
}
