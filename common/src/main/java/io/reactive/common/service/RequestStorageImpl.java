package io.reactive.common.service;

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


import io.reactive.common.domain.Request;
import io.reactive.common.util.GuavaCollectors;
import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.LongStream;

@Singleton
public class RequestStorageImpl implements RequestStorage {
    @NotNull
    @Override
    public List<Request> get(long n) {
        return LongStream.range(0, n)
            .boxed()
            .map(_ignored -> new Request())
            .collect(GuavaCollectors.toImmutableList());
    }
}
