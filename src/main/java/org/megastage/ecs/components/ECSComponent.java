package org.megastage.ecs.components;

import org.megastage.ecs.ECSEntity;
import org.megastage.ecs.ECSUtil;

import java.util.Map;

public interface ECSComponent {
    /** These are automatically overwritten in classes that are annotated with Component **/
    int cid = 0;
    default int cid() { return 0; }

    /** This method is called when initial state component is created from template **/
    default void config(int eid, Map<String, String> params, Map<String, ECSEntity> family) {
        try {
            ECSUtil.setConfig(this, params, family);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /** This method is called after world is ready **/
    default void initialize() {}

    /** This method is called when entities is deleted **/
    default void delete(int eid) {}
}
