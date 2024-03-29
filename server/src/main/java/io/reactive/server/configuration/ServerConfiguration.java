package io.reactive.server.configuration;

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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class ServerConfiguration {
    @Inject
    @Named("server.max.messages.in.flight")
    private int maxMessagesInFlight;

    @Inject
    @Named("server.messages.multiplier")
    private int messageMultiplier;

    @Inject
    @Named("server.max.messages")
    private int maxMessages;

    @Inject
    @Named("server.messages.generator.number")
    private int generatorMessages;

    @Inject
    @Named("server.messages.generator.period.seconds")
    private int generatorMessagesPeriod;

    @Inject
    @Named("server.messages.generator.batches")
    private boolean generatorWithBatches;

    public int getMaxMessagesInFlight() {
        return maxMessagesInFlight;
    }

    public int getMessageMultiplier() {
        return messageMultiplier;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public int getGeneratorMessages() {
        return generatorMessages;
    }

    public int getGeneratorMessagesPeriod() {
        return generatorMessagesPeriod;
    }

    public boolean isGeneratorWithBatches() {
        return generatorWithBatches;
    }
}
