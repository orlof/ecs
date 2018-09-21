package org.megastage.ecs.example;

import org.jdom2.Element;
import org.megastage.ecs.components.ECSMessageComponent;

public class ExampleComponent extends ECSMessageComponent {
    public String value;

    @Override
    public void config(int eid, Element config) {
        value = config.getAttributeValue("value");
    }
}
