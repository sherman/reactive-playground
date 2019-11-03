package io.reactive.server.util;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactive.server.domain.Message;
import io.reactive.server.domain.WebSocketMessage;
import io.reactive.server.util.json.JsonMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Long.parseLong;
import static java.util.regex.Pattern.compile;

@Singleton
public class WebSocketUtils {
    private static final Logger log = LoggerFactory.getLogger(WebSocketUtils.class);

    private static final Pattern userIdPattern = compile("userId=([0-9]+)");

    @Inject
    private JsonMapper mapper;

    @Nullable
    public Long getUserId(String queryString) {
        Matcher matcher = userIdPattern.matcher(queryString);
        if (!matcher.find()) {
            return null;
        }

        return parseLong(matcher.group(1));
    }

    public WebSocketMessage getMessage(@NotNull Message message) {
        try {
            return new WebSocketMessage(mapper.writeValueAsString(message), message.getHeader().getCreated().getMillis());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

