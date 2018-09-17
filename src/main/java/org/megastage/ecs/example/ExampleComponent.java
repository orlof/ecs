package org.megastage.ecs.example;

import org.jdom2.Element;
import org.megastage.ecs.components.ECSComponent;

public class ExampleComponent extends ECSComponent {
    public String value;

    @Override
    public void config(Element config) {
        value = config.getAttributeValue("value");
    }
}
