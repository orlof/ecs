package org.megastage.ecs;

import com.esotericsoftware.minlog.Log;
import org.jdom2.Element;
import org.megastage.ecs.components.ECSComponent;

public class ECSEntity {
    public int eid;
    public ECSComponent[] component;

    boolean allocated;

    public ECSEntity(int eid, int capacity) {
        this.eid = eid;
        component = new ECSComponent[capacity];
    }

    public ECSComponent addComponent(Element element) {
        try {
            Class clazz = Class.forName(element.getAttributeValue("type"));
            ECSComponent c = (ECSComponent) clazz.newInstance();

            c.config(eid, element);

            component[c.cid()] = c;

            return c;

        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            Log.error("Cannot create component", e);
            throw new ECSException(e);
        }
    }

    public boolean contains(int cid) {
        return component[cid] != null;
    }
}
