/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.common.breaker;

import org.opensearch.LegacyESVersion;
import org.opensearch.OpenSearchException;
import org.opensearch.common.io.stream.StreamInput;
import org.opensearch.common.io.stream.StreamOutput;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.rest.RestStatus;

import java.io.IOException;

/**
 * Exception thrown when the circuit breaker trips
 *
 * @opensearch.internal
 */
public class CircuitBreakingException extends OpenSearchException {

    private final long bytesWanted;
    private final long byteLimit;
    private final CircuitBreaker.Durability durability;

    public CircuitBreakingException(StreamInput in) throws IOException {
        super(in);
        byteLimit = in.readLong();
        bytesWanted = in.readLong();
        if (in.getVersion().onOrAfter(LegacyESVersion.V_7_0_0)) {
            durability = in.readEnum(CircuitBreaker.Durability.class);
        } else {
            durability = CircuitBreaker.Durability.PERMANENT;
        }
    }

    public CircuitBreakingException(String message, CircuitBreaker.Durability durability) {
        this(message, 0, 0, durability);
    }

    public CircuitBreakingException(String message, long bytesWanted, long byteLimit, CircuitBreaker.Durability durability) {
        super(message);
        this.bytesWanted = bytesWanted;
        this.byteLimit = byteLimit;
        this.durability = durability;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeLong(byteLimit);
        out.writeLong(bytesWanted);
        if (out.getVersion().onOrAfter(LegacyESVersion.V_7_0_0)) {
            out.writeEnum(durability);
        }
    }

    public long getBytesWanted() {
        return this.bytesWanted;
    }

    public long getByteLimit() {
        return this.byteLimit;
    }

    public CircuitBreaker.Durability getDurability() {
        return durability;
    }

    @Override
    public RestStatus status() {
        return RestStatus.TOO_MANY_REQUESTS;
    }

    @Override
    protected void metadataToXContent(XContentBuilder builder, Params params) throws IOException {
        builder.field("bytes_wanted", bytesWanted);
        builder.field("bytes_limit", byteLimit);
        builder.field("durability", durability);
    }
}
