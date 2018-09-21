package org.megastage.ecs.example;

import org.jdom2.Element;
import org.megastage.ecs.ECSEntity;
import org.megastage.ecs.components.ECSMessageComponent;

import java.util.Map;

public class ExampleComponent extends ECSMessageComponent {
    public String value;

    @Override
    public void config(int eid, Element config, Map<String, String> params, Map<String, ECSEntity> entityMap) {
        value = config.getAttributeValue("value");
    }
}
