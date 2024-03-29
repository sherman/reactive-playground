package io.reactive.server.app;

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

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.reactive.common.configuration.RootModule;
import io.reactive.server.UndertowWsServer;
import io.reactive.server.configuration.ActorModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UndertowWsServerApp {
    private static final Logger log = LoggerFactory.getLogger(UndertowWsServerApp.class);

    public static void main(String[] args) throws Exception {
        log.info("Starting application");

        log.info("Init application");
        try {
            Injector injector = Guice.createInjector(new RootModule(), new ActorModule());
            injector.getInstance(UndertowWsServer.class).start();
        } catch (Exception e) {
            log.error("Can't init reactive server", e);
            throw e;
        }
    }
}
