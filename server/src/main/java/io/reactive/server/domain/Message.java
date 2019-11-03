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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.reactive.server.util.json.RawObjectDeserializer;

import static com.google.common.base.Objects.equal;

public class Message {
    @JsonProperty(required = true)
    private Header header;

    @JsonRawValue
    @JsonDeserialize(using = RawObjectDeserializer.class)
    private String message;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (null == object) {
            return false;
        }

        if (!(object instanceof Message)) {
            return false;
        }

        Message o = (Message) object;

        return equal(header, o.header)
            && equal(message, o.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(header, message);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .addValue(header)
            .addValue(message)
            .toString();
    }
}
