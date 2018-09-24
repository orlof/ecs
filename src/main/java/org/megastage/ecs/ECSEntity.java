package org.megastage.ecs;

import com.esotericsoftware.minlog.Log;
import org.jdom2.Element;
import org.megastage.ecs.components.ECSComponent;

import java.util.Iterator;
import java.util.Map;

public class ECSEntity implements Iterable<ECSComponent> {
    public int eid;
    public ECSComponent[] component;

    boolean allocated;

    public ECSEntity(int eid, int capacity) {
        this.eid = eid;
        component = new ECSComponent[capacity];
    }

    public ECSComponent addComponent(Element element, Map<String, String> params, Map<String, ECSEntity> entityMap) {
        try {
            String name = element.getName();
            if(name.startsWith("ECS")) {
                name = String.format("org.megastage.ecs.components.%s", name);
            } else {
                name = String.format("org.megastage.components.%s", name);
            }

            Class clazz = Class.forName(name);
            ECSComponent c = (ECSComponent) clazz.newInstance();

            c.config(eid, element, params, entityMap);

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

    @Override
    public Iterator<ECSComponent> iterator() {
        return new ECSComponentIterator();
    }

    private class ECSComponentIterator implements Iterator<ECSComponent> {
        private int nextPosition = 0;

        ECSComponentIterator() {
            advance();
        }

        private void advance() {
            while(component[nextPosition] == null && nextPosition < component.length) {
                nextPosition++;
            }
        }

        public boolean hasNext() {
            return nextPosition < component.length;
        }

        public ECSComponent next() {
            int result = nextPosition;
            advance();
            return component[result];
        }
    }
}
