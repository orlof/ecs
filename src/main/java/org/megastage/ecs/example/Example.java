package org.megastage.ecs.example;

import org.jdom2.Element;
import org.megastage.ecs.ECSWorld;

public class Example {
    public static void main(String[] args) {
        ECSWorld world = new ECSWorld(1000, "org.megastage.ecs");

        world.add(new ExampleEntitySystem(world, 1000, ExampleComponent.cid));

        Element template = new Element("template").setAttribute("name", "ship")
                .addContent(new Element("entity").setAttribute("name", "ship")
                        .addContent(new Element("component")
                                .setAttribute("type", "org.megastage.ecs.example.ExampleComponent1")
                                .setAttribute("value", "1"))
                        .addContent(new Element("component")
                                .setAttribute("type", "org.megastage.ecs.example.ExampleComponentA")
                                .setAttribute("value", "A"))
                ).addContent(new Element("entity").setAttribute("name", "dcpu")
                        .addContent(new Element("component")
                                .setAttribute("type", "org.megastage.ecs.example.ExampleComponent1")
                                .setAttribute("value", "11"))
                        .addContent(new Element("component")
                                .setAttribute("type", "org.megastage.ecs.example.ExampleComponentA")
                                .setAttribute("value", "AA"))
                );

        world.add(template);
        world.spawn("ship");

    }
}
