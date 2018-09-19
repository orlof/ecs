package org.megastage.ecs.components;

import org.jdom2.Element;
import org.megastage.ecs.ECSException;
import org.megastage.ecs.ECSUtil;
import org.megastage.ecs.ECSWorld;

public abstract class ECSComponent {
    public static int cid = -1;
    public int eid;

    protected transient boolean replicated;
    protected transient boolean dirty;

    public int cid() {
        try {
            return getClass().getField("cid").getInt(this);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            throw new ECSException(e);
        }
    }

    /** This method is called when initial state component is created from template **/
    public void config(Element elem) {}

    /** This method is called after world is ready **/
    public void initialize() {
        if(getClass().getAnnotation(Component.class).replicated()) {
            replicated = true;
        }
    }

    /** This method is called when entity is deleted **/
    public void delete(int eid) {}

    public ECSComponent replicate() {
        if(replicated && dirty) {
            dirty = false;
            return this;
        }
        return null;
    }

    public void receive(ECSWorld world) {
        world.set(this);
    }

    public String toString() {
        return ECSUtil.toString(this);
    }
}
