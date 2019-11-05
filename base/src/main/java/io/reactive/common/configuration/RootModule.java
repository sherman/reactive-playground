package io.reactive.common.configuration;

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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.reactive.common.util.InjectorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class RootModule extends AbstractModule {
    private static final Logger log = LoggerFactory.getLogger(RootModule.class);

    public static final String SERVER_PROPERTIES = "application.properties";

    private final String propertyFileName = SERVER_PROPERTIES;

    @Override
    protected void configure() {
        requestStaticInjection(InjectorHolder.class);

        try {
            Names.bindProperties(binder(), loadProperties());
        } catch (Exception e) {
            log.error("Fatal error in the guice module", e);
            throw new RuntimeException(e);
        }
    }

    protected Properties loadProperties() throws Exception {
        return PropertiesLoader.loadProperties(propertyFileName, RootModule.class);
    }
}
