/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.semantics;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.semantics.model.Property;
import org.eclipse.smarthome.core.semantics.model.Tag;

/**
 * This interface defines a service, which offers functionality regarding semantic tags.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public interface TagService {

    /**
     * Retrieves the class for a given id.
     *
     *
     * @param tagId the id of the tag. The id can be fully qualified (e.g. "Location_Room_Bedroom") or a segment, if
     *            this uniquely identifies the tag
     *            (e.g. "Bedroom").
     * @return the class for the id or null, if non exists.
     */
    @Nullable
    Class<? extends Tag> byTagId(String tagId);

    /**
     * Determines the semantic entity type of an item, i.e. a sub-type of Location, Equipment or Point.
     *
     * @param item the item to get the semantic type for
     * @return a sub-type of Location, Equipment or Point
     */
    @Nullable
    Class<? extends Tag> getSemanticType(Item item);

    /**
     * Determines the Property that a Point relates to.
     *
     * @param item the item to get the property for
     * @return a sub-type of Property if the item represents a Point, otherwise null
     */
    @Nullable
    Class<? extends Property> getProperty(Item item);

}