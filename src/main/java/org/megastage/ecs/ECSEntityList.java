package org.megastage.ecs;

import java.util.Iterator;

class ECSEntityList implements Iterable<ECSEntity> {
    private final ECSWorld world;

    private ECSNode head = new ECSNode(null);
    private ECSNode[] node;
    public int size;

    ECSEntityList(ECSWorld world) {
        this.world = world;

        node = new ECSNode[world.entityCapacity];
        for(ECSEntity e: world) {
            node[e.eid] = new ECSNode(e);
        }
    }

    ECSEntity remove(ECSEntity entity) {
        size--;
        return node[entity.eid].remove().entity;
    }

    ECSEntity pop() {
        size--;
        return head.right.remove().entity;
    }

    ECSEntity push(ECSEntity entity) {
        size++;
        return node[entity.eid].appendTo(head).entity;
    }

    boolean contains(ECSEntity entity) {
        return node[entity.eid].selected;
    }

    @Override
    public Iterator<ECSEntity> iterator() {
        return new ECSEntityListIterator();
    }

    public Iterator<ECSEntity> pairinator() {
        return new ECSEntityListPairIterator();
    }

    private class ECSEntityListIterator implements Iterator<ECSEntity> {
        private ECSNode position = head;

        @Override
        public boolean hasNext() {
            return position.right != head;
        }

        @Override
        public ECSEntity next() {
            position = position.right;
            return position.entity;
        }
    }

    private class ECSEntityListPairIterator implements Iterator<ECSEntity> {
        private ECSNode left = head.right, right = head.right;

        @Override
        public boolean hasNext() {
            return size > 1 && (right.right != head || left.right != right);
        }

        @Override
        public ECSEntity next() {
            if(right.right == head) {
                left = left.right;
                right = left.right;
            } else {
                right = right.right;
            }
            return null;
        }

        public ECSEntity left() {
            return left.entity;
        }

        public ECSEntity right() {
            return right.entity;
        }
    }
}
