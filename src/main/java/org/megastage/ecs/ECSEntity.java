package org.megastage.ecs;

import com.esotericsoftware.minlog.Log;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.megastage.ecs.components.ECSComponent;

import java.util.HashMap;
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

    public boolean contains(int cid) {
        return component[cid] != null;
    }

    public void config(Element config, Map<String, String> params, Map<String, ECSEntity> family) {
        for(Element elem: config.getChildren()) {
            try {
                String name = elem.getName();
                if(name.startsWith("ECS")) {
                    name = String.format("org.megastage.ecs.components.%s", name);
                } else {
                    name = String.format("org.megastage.components.%s", name);
                }

                Class clazz = Class.forName(name);
                ECSComponent comp = (ECSComponent) clazz.newInstance();

                Map<String, String> attrs = new HashMap<>();
                for(Attribute attr: elem.getAttributes()) {
                    attrs.put(attr.getName(), params.getOrDefault(attr.getValue(), attr.getValue()));
                }

                comp.config(eid, attrs, family);

                component[comp.cid()] = comp;
            } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                Log.error("Cannot create component", e);
                throw new ECSException(e);
            }
        }
    }

    @Override
    public Iterator<ECSComponent> iterator() {
        return new ECSComponentIterator();
    }

    private class ECSComponentIterator implements Iterator<ECSComponent> {
        private int position = 0;

        ECSComponentIterator() {
            skip();
        }

        private void skip() {
            while(component[position] == null && position < component.length) {
                position++;
            }
        }

        public boolean hasNext() {
            return position < component.length;
        }

        public ECSComponent next() {
            ECSComponent comp = component[position++];
            skip();
            return comp;
        }
    }
}
