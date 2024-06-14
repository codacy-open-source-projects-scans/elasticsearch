/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.optimizer.rules;

import org.elasticsearch.xpack.esql.core.optimizer.OptimizerRules;
import org.elasticsearch.xpack.esql.core.plan.logical.LogicalPlan;
import org.elasticsearch.xpack.esql.core.plan.logical.UnaryPlan;
import org.elasticsearch.xpack.esql.optimizer.LogicalPlanOptimizer;

public final class PruneEmptyPlans extends OptimizerRules.OptimizerRule<UnaryPlan> {

    @Override
    protected LogicalPlan rule(UnaryPlan plan) {
        return plan.output().isEmpty() ? LogicalPlanOptimizer.skipPlan(plan) : plan;
    }
}
