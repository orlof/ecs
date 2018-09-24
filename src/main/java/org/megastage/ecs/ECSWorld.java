package org.megastage.ecs;

import javassist.*;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.megastage.ecs.components.AllocateCid;
import org.megastage.ecs.components.ECSComponent;

import java.util.*;

public class ECSWorld implements Iterable<ECSEntity> {
    final int entityCapacity;
    private final int componentCapacity;

    private ArrayList<ECSSystem> systems = new ArrayList<>(100);
    private Map<String, ECSEntityGroup> groups = new HashMap<>();
    private Map<String, Element> templates = new HashMap<>();

    public boolean dirty;
    public ECSEntity[] entities;

    private ECSEntityList free;

    // gameTime management
    private long tickCount = 0;
    private long gameTime = 0;
    private float deltaTimeForTick = 0.0f;
    private long deltaWallTimeToGameTime = 0;

    public ECSWorld(int size) {
        entityCapacity = size;
        componentCapacity = initComponents();

        entities = new ECSEntity[size];
        for(int eid=0; eid < size; eid++) {
            entities[eid] = new ECSEntity(eid, componentCapacity);
        }

        free = new ECSEntityList(this);
        for(ECSEntity entity: entities) {
            free.add(entity);
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
        templates.put(elem.getAttributeValue("name"), elem);
    }

    public ECSEntityGroup createGroup(int ...components) {
        ECSEntityGroup group = new ECSEntityGroup(this, components);

        groups.put(Arrays.toString(components), group);

        return group;
    }

    public ECSEntityGroup getGroup(int ...components) {
        return groups.get(Arrays.toString(components));
    }

    public ECSEntity allocateEntity() {
        ECSEntity entity = free.pop();
        entity.allocated = true;
        return entity;
    }

    public ECSEntity allocateEntity(int eid) {
        ECSEntity entity = entities[eid];
        entity.allocated = true;
        free.remove(entity);
        return entity;
    }

    public void freeEntity(ECSEntity e) {
        e.allocated = false;
        Arrays.fill(e.component, null);
        free.add(e);
    }

    public ECSComponent getComponent(int eid, int cid) {
        return entities[eid].component[cid];
    }

    public void setComponent(int eid, ECSComponent comp) {
        ECSEntity entity = entities[eid];
        if(!entity.allocated) {
            allocateEntity(entity.eid);
        }

        entity.component[comp.cid()] = comp;
        updateAllGroups(entity);
    }

    private void updateAllGroups(ECSEntity entity) {
        for(ECSEntityGroup group: groups.values()) {
            group.update(entity);
        }
    }

    public ECSEntity spawnInstance(String template, Map<String, String> params) {
        Map<String, ECSEntity> entityMap = new HashMap<>();
        subSpawn(template, params, entityMap);

        for(ECSEntity entity: entityMap.values()) {
            updateAllGroups(entity);
        }

        return entityMap.getOrDefault(template, null);
    }

    private void subSpawn(String templateName, Map<String, String> params, Map<String, ECSEntity> family) {
        Element root = templates.get(templateName);

        for(Element elem: root.getChildren()) {
            if(elem.getName().equals("entities")) {
                ECSEntity entity = allocateEntity();
                family.put(elem.getAttributeValue("name"), entity);
                entity.config(elem, params, family);
            } else if(elem.getName().equals("instance")) {
                String template = elem.getAttributeValue("template");
                HashMap<String, String> subParams = new HashMap<>();
                for(Attribute attr: elem.getAttributes()) {
                    subParams.put(attr.getName(), params.getOrDefault(attr.getValue(), attr.getValue()));
                }
                subSpawn(template, subParams, family);
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
            for(String classname: ECSUtil.annotated(AllocateCid.class)) {
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

    private class ECSEntityIterator implements Iterator<ECSEntity> {
        private int position = 0;

        public boolean hasNext() {
            return entities.length > position;
        }

        public ECSEntity next() {
            return entities[position++];
        }
    }
}
