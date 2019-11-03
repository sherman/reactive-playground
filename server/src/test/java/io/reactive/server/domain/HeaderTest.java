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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.reactive.server.configuration.RootModule;
import io.reactive.server.util.json.JsonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;

import static org.testng.Assert.assertEquals;

@Guice(modules = {RootModule.class})
public class HeaderTest {
    private static final Logger log = LoggerFactory.getLogger(HeaderTest.class);

    @Inject
    private JsonMapper objectMapper;

    @Test
    public void serialize() throws JsonProcessingException {
        Header header = new Header();
        header.setCreated(DateTime.parse("2018-01-14T13:27:52.178Z"));
        header.setMessageId("42");
        header.setMessageType(MessageType.HELLO_MESSAGE);
        header.setRecipient(42L);

        log.info("{}", objectMapper.writeValueAsString(header));

        assertEquals(objectMapper.writeValueAsString(header), "{\"messageId\":\"42\",\"messageType\":\"HELLO_MESSAGE\",\"recipient\":\"42\",\"created\":\"2018-01-14T13:27:52.178Z\"}");
    }

    @Test
    public void deserialize() throws IOException {
        Header header = new Header();
        header.setCreated(DateTime.parse("2018-01-14T13:27:52.178Z"));
        header.setMessageId("42");
        header.setMessageType(MessageType.HELLO_MESSAGE);
        header.setRecipient(42L);

        assertEquals(objectMapper.readValue("{\"messageId\":\"42\",\"messageType\":\"HELLO_MESSAGE\",\"recipient\":\"42\",\"created\":\"2018-01-14T13:27:52.178Z\"}", Header.class), header);
    }
}
