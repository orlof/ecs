package org.megastage.ecs;

import java.util.Iterator;

public class ECSEntityGroup implements Iterable<ECSEntity> {
    private ECSWorld world;

    private ECSEntityList entityList;
    private int[] requiredComponentIds;

    public String name;

    ECSEntityGroup(ECSWorld world, int[] requiredComponentIds) {
        this.world = world;
        this.requiredComponentIds = requiredComponentIds;

        entityList = new ECSEntityList(world);

        updateAll();
    }

    void updateAll() {
        for(ECSEntity entity: world) {
            update(entity);
        }
    }

    final void update(ECSEntity entity) {
        if(entity.allocated && match(entity)) {
            add(entity);
        } else {
            remove(entity);
        }
    }

    @Override
    public Iterator<ECSEntity> iterator() {
        return entityList.iterator();
    }

    public Iterator<ECSEntity> pairinator() {
        return entityList.pairinator();
    }

    private void add(ECSEntity entity) {
        if (!contains(entity)) {
            entityList.push(entity);
        }
    }

    private void remove(ECSEntity entity) {
        if(contains(entity)) {
            entityList.remove(entity);
        }
    }

    private boolean contains(ECSEntity entity) {
        return entityList.contains(entity);
    }

    private boolean match(ECSEntity entity) {
        for(int cid : requiredComponentIds) {
            if(entity.component[cid] == null) {
                return false;
            }
        }
        return true;
    }
}

