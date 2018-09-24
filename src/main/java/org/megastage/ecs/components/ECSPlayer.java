package org.megastage.ecs.components;

import org.megastage.ecs.ECSEntity;

import java.util.Map;

@KryoMessage
@AllocateCid
public class ECSPlayer extends ECSMessageComponent {
    public String nick;

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void config(int eid, Map<String, String> params, Map<String, ECSEntity> entityMap) {
        super.config(eid, params, entityMap);
    }
}
