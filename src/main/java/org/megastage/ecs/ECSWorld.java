package org.megastage.ecs;

import javassist.*;
import org.jdom2.Element;
import org.megastage.ecs.components.Component;
import org.megastage.ecs.components.ECSComponent;
import org.megastage.ecs.components.ECSMessageComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class ECSWorld implements Iterable<ECSEntity> {
    final int entityCapacity;
    private final int componentCapacity;

    private ArrayList<ECSSystem> systems = new ArrayList<>(100);
    private ArrayList<ECSEntityGroup> groups = new ArrayList<>(100);
    private HashMap<String, Element> templates = new HashMap<>();

    public ECSEntity[] entity;

    private ECSEntityList free;

    // gameTime management
    private long tickCount = 0;
    private long gameTime = 0;
    private float deltaTimeForTick = 0.0f;
    private long deltaWallTimeToGameTime = 0;

    public ECSWorld(int size) {
        entityCapacity = size;
        componentCapacity = initComponents();

        entity = new ECSEntity[size];
        for(int i=0; i<size; i++) {
            entity[i] = new ECSEntity(i, componentCapacity);
        }

        free = new ECSEntityList(this);
        for(ECSEntity e: entity) {
            free.add(e);
        }
    }

    public void initialize() {
        for(ECSSystem system: systems) {
            system.initialize();
        }
    }

    public void addSystem(ECSSystem system) {
        systems.add(system);
    }

    public void addEntityTemplate(Element elem) {
        if(elem.getName().equals("template")) {
            templates.put(elem.getAttributeValue("name"), elem);
        }
    }

    public ECSEntityGroup createGroup(int ...components) {
        ECSEntityGroup group = new ECSEntityGroup(this, components);
        groups.add(group);

        return group;
    }

    public ECSEntity allocateEntity() {
        ECSEntity entity = free.pop();
        entity.allocated = true;
        return entity;
    }

    public void freeEntity(ECSEntity e) {
        e.allocated = false;
        Arrays.fill(e.component, null);
        free.add(e);
    }




    public void set(ECSMessageComponent comp) {
        entity[comp.eid].component[comp.cid()] = comp;
    }

    public void spawn(String templateName) {
        Element templateElement = templates.get(templateName);

        for(Element entityElement: templateElement.getChildren("entity")) {
            ECSEntity entity = allocateEntity();

            for (Element componentElement : entityElement.getChildren("component")) {
                entity.addComponent(componentElement);
            }
        }
    }

    public void tick(long wallTime) {
        if(tickCount == 0) {
            deltaWallTimeToGameTime = -wallTime;
        }

        long nextGameTime = wallTime + deltaWallTimeToGameTime;
        deltaTimeForTick = (nextGameTime - gameTime) / 1000.0f;
        gameTime = nextGameTime;

        for(ECSSystem system: systems) {
            try {
                system.process(gameTime);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }

        tickCount++;
    }

    private int initComponents() {
        try {
            ClassPool cp = ClassPool.getDefault();

            int index = 0;
            for(String classname: ECSUtil.annotated(Component.class)) {
                CtClass ctClass = cp.get(classname);
                ctClass.addField(CtField.make(String.format("public static int cid = %d;", index++), ctClass));
                ctClass.addMethod(CtNewMethod.make(String.format("public int cid() { return %d; }", index++), ctClass));
                ctClass.toClass();
            }

            return index;
        } catch (CannotCompileException | NotFoundException e) {
            e.printStackTrace();
            throw new ECSException(e);
        }
    }

    @Override
    public Iterator<ECSEntity> iterator() {
        return new ECSEntityIterator();
    }

    public ECSComponent getComponent(int eid, int cid) {
        return entity[eid].component[cid];
    }

    public void setComponent(int eid, ECSComponent comp) {
        entity[eid].component[comp.cid()] = comp;
    }

    private class ECSEntityIterator implements Iterator<ECSEntity> {
        private int position = 0;

        public boolean hasNext() {
            return entity.length > position;
        }

        public ECSEntity next() {
            return entity[position++];
        }
    }
}
