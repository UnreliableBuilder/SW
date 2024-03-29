/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.skywalking.oap.server.library.client.elasticsearch;

import java.util.Map;
import java.util.Optional;

import lombok.Getter;
import org.apache.skywalking.library.elasticsearch.requests.IndexRequest;
import org.apache.skywalking.oap.server.library.client.request.InsertRequest;

@Getter
public class IndexRequestWrapper implements InsertRequest {
    protected IndexRequest request;

    public IndexRequestWrapper(String index, String type, String id,
                               Map<String, ?> source) {
        this(index, type, id, Optional.empty(), source);
    }

    public IndexRequestWrapper(String index, String type, String id,
                               Optional<String> routing,
                               Map<String, ?> source) {
        request = IndexRequest.builder()
                              .index(index)
                              .type(type)
                              .id(id)
                              .routing(routing)
                              .doc(source)
                              .build();
    }

    /**
     * Expose an empty constructor to lazy initialization.
     */
    protected IndexRequestWrapper() {

    }

    @Override
    public void onInsertCompleted() {
    }
}
