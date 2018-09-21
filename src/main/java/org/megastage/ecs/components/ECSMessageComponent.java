package org.megastage.ecs.components;

import org.jdom2.Element;
import org.megastage.ecs.ECSUtil;
import org.megastage.ecs.ECSWorld;
import org.megastage.ecs.messages.ECSMessage;

public abstract class ECSMessageComponent implements ECSMessage, ECSComponent {
    public int eid;

    public void config(int eid, Element config) {
        this.eid = eid;
    }

    public ECSMessage transmit() {
        return this;
    }

    public void receive(ECSWorld world) {
        world.setComponent(eid, this);
    }

    public String toString() {
        return ECSUtil.toString(this);
    }
}
