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
 *
 */

package org.apache.skywalking.oap.server.core.config;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.server.library.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * Load settings from component-libraries.yml this file includes all component defines, and the component mappings,
 * which declare the real server type based on client component.
 */
@Slf4j
public class ComponentLibraryCatalogService implements IComponentLibraryCatalogService {
    private static final String COMPONENT_SERVER_MAPPING_SECTION = "Component-Server-Mappings";

    private Map<String, Integer> componentName2Id;
    private Map<Integer, String> componentId2Name;
    private Map<Integer, Integer> componentId2ServerId;
    /**
     * Key is the component ID.
     * Value is the priority.
     */
    private Map<Integer, Integer> componentIDPriorities;

    public ComponentLibraryCatalogService() throws InitialComponentCatalogException {
        init();
    }

    @Override
    public int getComponentId(String componentName) {
        return componentName2Id.get(componentName);
    }

    @Override
    public int getServerIdBasedOnComponent(int componentId) {
        Integer serverComponentId = componentId2ServerId.get(componentId);
        return serverComponentId == null ? componentId : serverComponentId;
    }

    @Override
    public String getComponentName(int componentId) {
        String componentName = componentId2Name.get(componentId);

        return componentName == null ? componentId2Name.get(0) : componentName;
    }

    @Override
    public String getServerNameBasedOnComponent(int componentId) {
        Integer serverComponentId = componentId2ServerId.get(componentId);
        return serverComponentId == null ? getComponentName(componentId) : getComponentName(serverComponentId);
    }

    private void init() throws InitialComponentCatalogException {
        componentName2Id = new HashMap<>();
        componentName2Id.put("N/A", 0);
        componentId2Name = new HashMap<>();
        componentId2Name.put(0, "N/A");
        componentId2ServerId = new HashMap<>();
        componentIDPriorities = new HashMap<>();

        Map<String, String> nameMapping = new HashMap<>();
        try {
            Reader applicationReader = ResourceUtils.read("component-libraries.yml");
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(applicationReader, Map.class);

            map.forEach((componentName, settingCollection) -> {
                Map settings = (Map) settingCollection;
                if (COMPONENT_SERVER_MAPPING_SECTION.equals(componentName)) {
                    settings.forEach((name, serverName) -> {
                        nameMapping.put((String) name, (String) serverName);
                    });
                } else {
                    Integer componentId = (Integer) settings.get("id");
                    componentName2Id.put((String) componentName, componentId);
                    componentId2Name.put(componentId, (String) componentName);
                    Integer priority = (Integer) settings.get("priority");
                    if (priority == null) {
                        priority = 50;
                    }
                    componentIDPriorities.put(componentId, priority);
                }
            });

            nameMapping.forEach((name, serverName) -> {
                if (!componentName2Id.containsKey(name)) {
                    throw new InitialComponentCatalogException(
                        "Component name [" + name + "] in Component-Server-Mappings doesn't exist in component define. ");
                }
                if (!componentName2Id.containsKey(serverName)) {
                    throw new InitialComponentCatalogException(
                        "Server componentId name [" + serverName + "] in Component-Server-Mappings doesn't exist in component define. ");
                }

                componentId2ServerId.put(componentName2Id.get(name), componentName2Id.get(serverName));
            });
            nameMapping.clear();
        } catch (FileNotFoundException e) {
            log.error("component-libraries.yml not found.", e);
        }

    }

    /**
     * @return true if the given componentB has high priority
     */
    public boolean compare(int componentA, int componentB) {
        final Integer priorityA = componentIDPriorities.getOrDefault(componentA, 50);
        final Integer priorityB = componentIDPriorities.getOrDefault(componentB, 50);
        return priorityA.compareTo(priorityB) < 0;
    }
}
