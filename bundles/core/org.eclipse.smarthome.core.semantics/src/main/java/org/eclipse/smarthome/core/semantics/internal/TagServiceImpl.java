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
package org.eclipse.smarthome.core.semantics.internal;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.semantics.TagService;
import org.eclipse.smarthome.core.semantics.model.Point;
import org.eclipse.smarthome.core.semantics.model.Property;
import org.eclipse.smarthome.core.semantics.model.Tag;
import org.eclipse.smarthome.core.semantics.model.TagInfo;
import org.eclipse.smarthome.core.semantics.model.equipment.Equipments;
import org.eclipse.smarthome.core.semantics.model.location.Locations;
import org.eclipse.smarthome.core.semantics.model.point.Points;
import org.eclipse.smarthome.core.semantics.model.property.Properties;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

/**
 * The internal implementation of the {@link TagService} interface, which is registered as an OSGi service.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@Component
public class TagServiceImpl implements TagService {

    private final Map<String, Class<? extends Tag>> tagSets = new TreeMap<>();

    protected void activate(BundleContext context) {
        Locations.stream().forEach(l -> addTagSet(l));
        Equipments.stream().forEach(l -> addTagSet(l));
        Points.stream().forEach(l -> addTagSet(l));
        Properties.stream().forEach(l -> addTagSet(l));
    }

    @Override
    public Class<? extends Tag> byTagId(String tagSet) {
        return tagSets.get(tagSet);
    }

    private void addTagSet(Class<? extends Tag> tagSet) {
        String id = tagSet.getAnnotation(TagInfo.class).id();
        while (id.indexOf("_") != -1) {
            this.tagSets.put(id, tagSet);
            id = id.substring(id.indexOf("_") + 1);
        }
        this.tagSets.put(id, tagSet);
    }

    @Override
    public Class<? extends Tag> getSemanticType(Item item) {
        Set<String> tags = item.getTags();
        for (String tag : tags) {
            Class<? extends Tag> type = byTagId(tag);
            if (type != null && !Property.class.isAssignableFrom(type)) {
                return type;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Property> getProperty(Item item) {
        if (Point.class.isAssignableFrom(getSemanticType(item))) {
            Set<String> tags = item.getTags();
            for (String tag : tags) {
                Class<? extends Tag> type = byTagId(tag);
                if (Property.class.isAssignableFrom(type)) {
                    return (Class<? extends Property>) type;
                }
            }
        }
        return null;
    }
}
