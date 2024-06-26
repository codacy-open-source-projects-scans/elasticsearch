/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.core.slm.action;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionType;
import org.elasticsearch.action.support.master.AcknowledgedRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.xpack.core.slm.SnapshotLifecyclePolicy;

import java.io.IOException;
import java.util.Objects;

public class PutSnapshotLifecycleAction extends ActionType<AcknowledgedResponse> {
    public static final PutSnapshotLifecycleAction INSTANCE = new PutSnapshotLifecycleAction();
    public static final String NAME = "cluster:admin/slm/put";

    protected PutSnapshotLifecycleAction() {
        super(NAME);
    }

    public static class Request extends AcknowledgedRequest<Request> {

        private final String lifecycleId;
        private final SnapshotLifecyclePolicy lifecycle;

        public Request(TimeValue masterNodeTimeout, TimeValue ackTimeout, String lifecycleId, SnapshotLifecyclePolicy lifecycle) {
            super(masterNodeTimeout, ackTimeout);
            this.lifecycleId = lifecycleId;
            this.lifecycle = lifecycle;
        }

        public Request(StreamInput in) throws IOException {
            super(in);
            lifecycleId = in.readString();
            lifecycle = new SnapshotLifecyclePolicy(in);
        }

        public String getLifecycleId() {
            return this.lifecycleId;
        }

        public SnapshotLifecyclePolicy getLifecycle() {
            return this.lifecycle;
        }

        public static Request parseRequest(TimeValue masterNodeTimeout, TimeValue ackTimeout, String lifecycleId, XContentParser parser) {
            return new Request(masterNodeTimeout, ackTimeout, lifecycleId, SnapshotLifecyclePolicy.parse(parser, lifecycleId));
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeString(lifecycleId);
            lifecycle.writeTo(out);
        }

        @Override
        public ActionRequestValidationException validate() {
            return lifecycle.validate();
        }

        @Override
        public int hashCode() {
            return Objects.hash(lifecycleId, lifecycle);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Request other = (Request) obj;
            return lifecycleId.equals(other.lifecycleId) && lifecycle.equals(other.lifecycle);
        }

        @Override
        public String toString() {
            return Strings.toString((b, p) -> b.field(lifecycleId, lifecycle));
        }
    }
}
