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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Map;

import static com.google.common.base.Objects.equal;

public class Header implements Serializable {
    @JsonProperty(required = true)
    private String messageId;
    @JsonProperty(required = true)
    private MessageType messageType;

    // required only for outgoing messaging
    private Long recipient;

    // just a copy from message id timestamp part
    @JsonProperty(required = true)
    private DateTime created;

    @JsonIgnore
    private Map<String, String> mdcContext;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Long getRecipient() {
        return recipient;
    }

    public void setRecipient(Long recipient) {
        this.recipient = recipient;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }

        if (!(object instanceof Header)) {
            return false;
        }

        Header o = (Header) object;

        return equal(messageId, o.messageId)
            && equal(messageType, o.messageType)
            && equal(recipient, o.recipient)
            && equal(created, o.created);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(messageId, messageType, recipient, created);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .addValue(messageId)
            .addValue(messageType)
            .addValue(recipient)
            .addValue(created)
            .toString();
    }
}
