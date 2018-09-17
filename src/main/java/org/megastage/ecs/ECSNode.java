package org.megastage.ecs;

class ECSNode {
    ECSEntity entity;

    ECSNode left, right;
    boolean selected;

    ECSNode(ECSEntity e) {
        entity = e;
        left = right = this;
    }

    ECSNode appendTo(ECSNode n) {
        left = n;
        right = n.right;

        right.left = this;
        left.right = this;

        selected = true;

        return this;
    }

    ECSNode remove() {
        left.right = right;
        right.left = left;

        selected = false;

        return this;
    }
}
