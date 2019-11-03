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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class Demand {
    private static final Logger log = LoggerFactory.getLogger(Demand.class);

    private final AtomicLong demand = new AtomicLong();

    /**
     * @param delta
     * @return total demand
     */
    public long increase(long delta) {
        Preconditions.checkArgument(delta > 0, "Delta MUST BE positive!");

        long newDemand = demand.addAndGet(delta);
        log.trace("Demand: [{}], delta: [{}]", newDemand, delta);
        return newDemand;
    }

    /**
     * @param delta
     * @return decreased demand
     */
    public long decrease(long delta) {
        Preconditions.checkArgument(delta > 0, "Delta MUST BE positive!");

        for (;;) {
            long current = demand.get();
            long newValue = Math.min(current, delta);

            if (demand.compareAndSet(current, current - newValue)) {
                return newValue;
            }
        }
    }

    public void reset() {
        demand.set(0);
    }

    public long current() {
        return demand.get();
    }

    public boolean isFulfilled() {
        return current() == 0;
    }
}
