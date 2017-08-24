/**
 * Copyright (c) 1997, 2015 by ProSyst Software GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation;

import java.util.Collections;
import java.util.Map;

import org.eclipse.smarthome.automation.handler.ConditionHandler;
import org.eclipse.smarthome.automation.type.ConditionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.Configuration;

/**
 * Condition module is used into "IF" section of the {@link Rule} definition.
 * The "IF" section defines conditions which must be satisfied to continue {@link Rule} execution. Building elements of
 * condition ( {@link ConfigDescriptionParameter}s and {@link Input}s are defined by {@link ConditionType} Conditions
 * don't have {@link Output} elements.
 *
 * @author Yordan Mihaylov - Initial Contribution
 */
public class Condition extends Module {

    private Map<String, String> inputs;

    /**
     * Whether the condition should be negated when evaluated.
     */
    private boolean negated;

    public Condition() {
        super();
    }

    /**
     * Constructor of {@link Condition} module object.
     *
     * @param id id of the module.
     * @param typeUID unique module type id.
     * @param configuration configuration values of the {@link Condition} module.
     * @param inputs set of {@link Input}s used by this module.
     */
    public Condition(String id, String typeUID, Configuration configuration, Map<String, String> inputs) {
        super(id, typeUID, configuration);
        setInputs(inputs);
        setNegated(false);
    }

    /**
     * This method is used to get input connections of the Condition. The connections
     * are links between {@link Input}s of the current {@link Module} and {@link Output}s of other
     * {@link Module}s.
     *
     * @return map that contains the inputs of this condition.
     */
    public Map<String, String> getInputs() {
        return inputs != null ? inputs : Collections.<String, String> emptyMap();
    }

    /**
     * This method is used to connect {@link Input}s of the Condition to {@link Output}s of other {@link Module}s.
     *
     * @param inputs map that contains the inputs for this condition.
     */
    public void setInputs(Map<String, String> inputs) {
        if (inputs != null) {
            this.inputs = inputs;
        }
    }

    /**
     * This method is used to get the negated status of this Condition. The result of
     * {@link ConditionHandler#isSatisfied(Map)} will be negated by the {@link RuleEngine} if the Condition is negated,
     * preventing a {@link Rule} from being run if the condition is satisfied.
     *
     * @return true if the condition is negated, false otherwise.
     */
    public boolean getNegated() {
        return negated;
    }

    /**
     * This method is used to set the negated status of the Condition.
     *
     * @param negated boolean the negated status
     */
    public void setNegated(boolean negated) {
        this.negated = negated;
    }

}
