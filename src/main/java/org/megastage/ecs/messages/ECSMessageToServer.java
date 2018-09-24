package org.megastage.ecs.messages;

import com.esotericsoftware.kryonet.network.messages.MessageToServer;
import org.megastage.ecs.ECSConnectionToClient;
import org.megastage.ecs.ECSWorld;

public interface ECSMessageToServer extends MessageToServer {
    void receive(ECSWorld world, ECSConnectionToClient connection);
}
