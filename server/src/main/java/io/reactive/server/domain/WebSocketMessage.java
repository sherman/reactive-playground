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

import java.io.Serializable;

public class WebSocketMessage implements Serializable {
    private final String payload;
    private final long timestamp;

    // options for sender
    protected transient boolean expirable = true;
    private transient RecipientMode mode;
    private transient String sessionId;

    public WebSocketMessage(String payload, long timestamp) {
        this.payload = payload;
        this.timestamp = timestamp;
        // by default, send a message to all connections
        this.mode = RecipientMode.ALL;
    }

    public String getPayload() {
        return payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RecipientMode getMode() {
        return mode;
    }

    public void setMode(RecipientMode mode) {
        this.mode = mode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isExpirable() {
        return expirable;
    }
}
