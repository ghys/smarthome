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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.items.Item;
import org.eclipse.smarthome.core.semantics.model.Property;
import org.eclipse.smarthome.core.semantics.model.Tag;
import org.eclipse.smarthome.core.semantics.model.TagInfo;
import org.eclipse.smarthome.core.semantics.model.equipment.Equipments;
import org.eclipse.smarthome.core.semantics.model.equipment.Sensor;
import org.eclipse.smarthome.core.semantics.model.location.Locations;
import org.eclipse.smarthome.core.semantics.model.point.Command;
import org.eclipse.smarthome.core.semantics.model.point.Points;
import org.eclipse.smarthome.core.semantics.model.property.Properties;
import org.eclipse.smarthome.core.types.StateDescription;

/**
 * This is a class that gives static access to the semantic tag library.
 * For everything that is not static, the {@link SemanticsService} should be used instead.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class SemanticTags {

    private static String TAGS_BUNDLE_NAME = "tags";

    private static final Map<String, Class<? extends Tag>> TAGS = new TreeMap<>();

    static {
        Locations.stream().forEach(l -> addTagSet(l));
        Equipments.stream().forEach(l -> addTagSet(l));
        Points.stream().forEach(l -> addTagSet(l));
        Properties.stream().forEach(l -> addTagSet(l));
    }

    /**
     * Retrieves the class for a given id.
     *
     * @param tagId the id of the tag. The id can be fully qualified (e.g. "Location_Room_Bedroom") or a segment, if
     *            this uniquely identifies the tag
     *            (e.g. "Bedroom").
     * @return the class for the id or null, if non exists.
     */
    @Nullable
    public static Class<? extends Tag> getById(String tagId) {
        return TAGS.get(tagId);
    }

    @Nullable
    public static Class<? extends Tag> getByLabel(String tagLabel, Locale locale) {
        return TAGS.values().stream().distinct().filter(t -> getLabel(t, locale).equalsIgnoreCase(tagLabel)).findFirst()
                .orElse(null);
    }

    @Nullable
    public static Class<? extends Tag> getByLabelOrSynonym(String tagLabelOrSynonym, Locale locale) {
        return TAGS.values().stream().distinct()
                .filter(t -> getLabelAndSynonyms(t, locale).contains(tagLabelOrSynonym.toLowerCase(locale))).findFirst()
                .orElse(null);
    }

    public static List<String> getLabelAndSynonyms(Class<? extends Tag> tag, Locale locale) {
        ResourceBundle rb = ResourceBundle.getBundle(TAGS_BUNDLE_NAME, locale);
        try {
            String entry = rb.getString(tag.getAnnotation(TagInfo.class).id());
            return Arrays.asList(entry.toLowerCase(locale).split(","));
        } catch (MissingResourceException e) {
            return Collections.singletonList(tag.getAnnotation(TagInfo.class).label());
        }
    }

    public static String getLabel(Class<? extends Tag> tag, Locale locale) {
        ResourceBundle rb = ResourceBundle.getBundle(TAGS_BUNDLE_NAME, locale);
        try {
            String entry = rb.getString(tag.getAnnotation(TagInfo.class).id());
            if (entry.contains(",")) {
                return entry.substring(0, entry.indexOf(","));
            } else {
                return entry;
            }
        } catch (MissingResourceException e) {
            return tag.getAnnotation(TagInfo.class).label();
        }
    }

    /**
     * Determines the semantic entity types of an item, i.e. sub-types of Location, Equipment or Point.
     *
     * @param item the item to get the semantic type for
     * @return a list of sub-types of Location, Equipment or Point
     */
    @NonNull
    public static Set<Class<? extends @NonNull Tag>> getSemanticTypes(Item item) {
        Set<String> tags = item.getTags();
        Set<Class<? extends @NonNull Tag>> types = new HashSet<Class<? extends @NonNull Tag>>();
        for (String tag : tags) {
            Class<? extends Tag> type = getById(tag);
            if (type != null && !Property.class.isAssignableFrom(type)) {
                types.add(type);
            }
        }
        // we haven't found any type as a tag, but if there is a Property tag, we can conclude that it is a Point
        if (types.isEmpty() && !getProperties(item).isEmpty()) {
            StateDescription stateDescription = item.getStateDescription();
            if (stateDescription != null && stateDescription.isReadOnly()) {
                types.add(Sensor.class);
            } else {
                types.add(Command.class);
            }
        }

        return types;
    }

    /**
     * Determines the Properties that a Point relates to.
     *
     * @param item the item to get the properties for
     * @return the set of sub-types of Property if the item represents a Point
     */
    @SuppressWarnings("unchecked")
    @NonNull
    public static Set<Class<? extends @NonNull Property>> getProperties(Item item) {
        Set<Class<? extends @NonNull Property>> properties = new HashSet<Class<? extends @NonNull Property>>();
        Set<String> tags = item.getTags();
        for (String tag : tags) {
            Class<? extends Tag> type = getById(tag);
            if (type != null && Property.class.isAssignableFrom(type)) {
                properties.add((Class<? extends Property>) type);
            }
        }
        return properties;
    }

    private static void addTagSet(Class<? extends Tag> tagSet) {
        String id = tagSet.getAnnotation(TagInfo.class).id();
        while (id.indexOf("_") != -1) {
            TAGS.put(id, tagSet);
            id = id.substring(id.indexOf("_") + 1);
        }
        TAGS.put(id, tagSet);
    }
}
