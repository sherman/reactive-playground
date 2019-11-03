package io.reactive.server.domain;

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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;

import static com.google.common.collect.Maps.uniqueIndex;
import static io.reactive.server.domain.MessageType.DirectionType.OUTGOING;

public enum MessageType {
    HELLO_MESSAGE(true, OUTGOING);

    private final boolean supportHistory;
    private final DirectionType type;

    MessageType(boolean supportHistory, DirectionType type) {
        this.supportHistory = supportHistory;
        this.type = type;
    }

    public boolean isSupportHistory() {
        return supportHistory;
    }

    @NotNull
    public static MessageType fromOrdinal(int ordinal) {
        return ordinalLookup.get(ordinal);
    }

    private static final Map<Integer, MessageType> ordinalLookup = uniqueIndex(
        Arrays.asList(MessageType.values()),
        Enum::ordinal
    );

    public DirectionType getType() {
        return type;
    }

    public enum DirectionType {
        INCOMING,
        OUTGOING
    }
}
