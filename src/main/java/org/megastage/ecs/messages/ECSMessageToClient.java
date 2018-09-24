package org.megastage.ecs.messages;

import com.esotericsoftware.kryonet.network.ServerConnection;
import com.esotericsoftware.kryonet.network.messages.MessageToClient;
import org.megastage.ecs.ECSWorld;

public interface ECSMessageToClient extends MessageToClient {
    void receive(ECSWorld world, ServerConnection connection);
}
