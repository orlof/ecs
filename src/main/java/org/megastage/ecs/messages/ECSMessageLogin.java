package org.megastage.ecs.messages;

import org.megastage.ecs.ECSConnectionToClient;
import org.megastage.ecs.ECSWorld;
import org.megastage.ecs.components.KryoMessage;

@KryoMessage
public class ECSMessageLogin implements ECSMessageToServer {
    public String nick;

    @Override
    public void receive(ECSWorld world, ECSConnectionToClient connection) {
        if(connection.state == ECSConnectionToClient.State.WaitingForLogin) {
            connection.nick = nick;
            connection.state = ECSConnectionToClient.State.WaitingForInitialData;
        }
    }
}
