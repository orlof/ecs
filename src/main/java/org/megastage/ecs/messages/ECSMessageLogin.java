package org.megastage.ecs.messages;

import org.megastage.ecs.components.ECSBindTo;
import org.megastage.ecs.components.ECSPosition;
import org.megastage.components.SpawnPoint;
import org.megastage.ecs.ECSConnection;
import org.megastage.ecs.ECSEntity;
import org.megastage.ecs.ECSEntityGroup;
import org.megastage.ecs.ECSWorld;
import org.megastage.ecs.components.KryoMessage;

import java.util.HashMap;
import java.util.Map;

@KryoMessage
public class ECSMessageLogin implements ECSMessage {
    public String nick;

    @Override
    public void receive(ECSWorld world, ECSConnection conn) {
        ECSEntityGroup spawnPoints = world.getGroup(SpawnPoint.cid);
        for(ECSEntity spawnPointEntity: spawnPoints) {
            SpawnPoint spawnPoint = (SpawnPoint) spawnPointEntity.component[SpawnPoint.cid];

            if(!spawnPoint.allocated) {
                spawnPoint.allocated = true;

                Map<String, String> params = new HashMap<>();
                params.put("NICK", nick);

                ECSPosition pos = ((ECSPosition) spawnPointEntity.component[ECSPosition.cid]);
                params.put("POS_VECTOR", String.format("%f %f %f", pos.value.x, pos.value.y, pos.value.z));

                ECSBindTo bindTo = ((ECSBindTo) spawnPointEntity.component[ECSBindTo.cid]);
                params.put("MASTER", String.valueOf(bindTo.master));

                conn.player = world.spawnInstance("player", params);

                world.dirty = true;

                return;
            }
        }
    }
}
