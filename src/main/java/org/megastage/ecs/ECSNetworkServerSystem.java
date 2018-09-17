package org.megastage.ecs;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import com.esotericsoftware.minlog.Log;
import com.google.common.graph.Network;
import javassist.*;
import org.megastage.ecs.components.Component;
import org.megastage.ecs.components.ECSFlagDeleted;
import org.megastage.ecs.components.ECSFlagDistributed;
import org.megastage.ecs.components.ECSFlagPlayer;
import org.megastage.ecs.messages.ECSMessageLogin;

import java.io.IOException;

public class ECSNetworkServerSystem extends ECSSystem {
    private Server server;

    private ECSEntityGroup replicate;
    private ECSEntityGroup delete;
    private ECSEntityGroup player;

    public ECSNetworkServerSystem(ECSWorld world, long interval) {
        super(world, interval);

        replicate = world.createGroup(ECSFlagDistributed.cid);
        delete = world.createGroup(ECSFlagDeleted.cid);
        player = world.createGroup(ECSFlagPlayer.cid);
    }

    @Override
    public void initialize() {
        server = new Server(64*1024, 64*1024) {
            @Override
            protected Connection newConnection () {
                return new ECSNetworkConnection();
            }
        };

        ECSUtil.registerKryoClasses(server.getKryo());

        server.addListener(new NetworkListener());
        new Thread(server).start();

        try {
            server.bind(57463, 57464);
        } catch (IOException e) {
            e.printStackTrace();
            throw new ECSException(e);
        }
    }

    @Override
    protected void process() {
        Connection[] connections = server.getConnections();
        processNewConnections(connections);

        Bag<Message> update = new Bag<>(100);
        update.add(new TimestampMessage());

        processDeletedEntities(update);
        processSynchronizedEntities(update);

        if(update.size() > 1) {
            Message[] data = update.toArray(Message.class);

            for(Connection c: connections) {
                //Log.info("sending %d messages", data.length);
                c.sendUDP(data);
            }
        }
    }

    private void initConnection(PlayerConnection connection) {
        int eid = 0;
        for(eid = characters.iterator(); eid > 0; eid = characters.next()) {
            PlayerCharacter pc = (PlayerCharacter) World.INSTANCE.getComponent(eid, CompType.PlayerCharacter);
            if(!pc.allocated && pc.name.equalsIgnoreCase(connection.nick)) {
                break;
            }
        }

        if(eid == 0) {
            eid = createNewCharacter(connection);
        }

        selectCharacter(connection, eid);
    }

    private void selectCharacter(PlayerConnection connection, int eid) {
        connection.player = eid;

        PlayerCharacter pc = (PlayerCharacter) World.INSTANCE.getComponent(eid, CompType.PlayerCharacter);
        pc.allocated = true;

        world.setComponent(connection.player, CompType.CmdText, CmdText.create("joined"));

        connection.sendTCP(new PlayerIDMessage(eid));
    }

    private int createNewCharacter(PlayerConnection connection) {
        // create ship
        int ship = TemplateManager.create(world, "Apollo 13");

        Position shipPos = (Position) world.getComponent(ship, CompType.Position);
        shipPos.move(ServerGlobals.shipStartVec);
        ServerGlobals.advanceShipStartVec();

        // create character
        int eid = TemplateManager.create(world, "Player");

        // bind player to ship
        BindTo bind = new BindTo();
        bind.parent = ship;
        world.setComponent(eid, CompType.BindTo, bind);

        SpawnPoint sp = (SpawnPoint) world.getComponent(ship, CompType.SpawnPoint);

        Position pos = (Position) world.getComponent(eid, CompType.Position);
        pos.set(sp.vector);

        PlayerCharacter pc = (PlayerCharacter) world.getComponent(eid, CompType.PlayerCharacter);
        pc.name = connection.nick;

        return eid;
    }

    private int probe(float pos, float step) {
        float target = pos + step + Math.signum(step) * 0.3f;
        if(target < 0.0f) target -= 1.0f;

        int start = (int) pos;
        int end = (int) target;

        if(end < start - 1) end = start - 1;
        if(end > start + 1) end = start + 1;

        return end;
    }

    private boolean blocked(Ship ship, int x, int y, int z) {
        return ship.getBlock(x, y, z) != 0 || ship.getBlock(x, y+1, z) != 0 ||
                ship.getBlock(x, y-1, z) != '#';
    }

    private int collisionXZ(Ship ship, int cx, int cy, int cz, int px, int py, int pz) {
        int result = 0;
        if(cx != px && blocked(ship, px, cy, cz)) {
            result |= 1;
        }
        if(cz != pz && blocked(ship, cx, cy, pz)) {
            result |= 2;
        }
        if(cx != px && cz != pz && blocked(ship, px, cy, pz)) {
            result |= 3;
        }
        return result;
    }


    private void unpickItem(PlayerConnection connection, UserCommand cmd) {
        connection.item = -1;
        Mode mode = (Mode) world.getComponent(connection.player, CompType.Mode);
        mode.setMode(ClientMode.WALK);
    }

    private void pickItem(PlayerConnection connection, UserCommand cmd) {
        int target = cmd.pick.eid;
        if(!world.hasEntity(target)) {
            Log.error("No such item to pick: " + target);
            return;
        }

        // TODO check distance
        // Position pos = connection.player.getComponent(Position.class);
        // ===================

        DeviceMonitor mon = (DeviceMonitor) world.getComponent(target, CompType.VirtualMonitor);
        if(mon != null) {
            DCPU dcpu = (DCPU) world.getComponent(mon.dcpuEID, CompType.DCPU);

            for(int i=0; i < dcpu.hardwareSize; i++) {
                InterfaceKeyboard kbd = (InterfaceKeyboard) world.getComponent(dcpu.hardware[i], CompType.VirtualKeyboard);
                if(kbd != null) {
                    connection.item = dcpu.hardware[i];
                    Mode mode = (Mode) world.getComponent(connection.player, CompType.Mode);
                    mode.setMode(ClientMode.DCPU);
                    return;
                }
            }
            return;
        }

        InterfaceFloppyDrive fd = (InterfaceFloppyDrive) world.getComponent(target, CompType.VirtualFloppyDrive);
        if(fd != null) {
            connection.item = target;
            Mode mode = (Mode) world.getComponent(connection.player, CompType.Mode);
            mode.setMode(ClientMode.MENU);
            return;
        }
    }

    private void updatePlayerPosition(Ship ship, int player, UserCommand cmd) {
        Position pos = (Position) world.getComponent(player, CompType.Position);

        Vector3f coord = pos.get();
        int cx = (int) coord.x;
        int cy = (int) coord.y;
        int cz = (int) coord.z;

        int xprobe = probe(coord.x, cmd.move.x);
        int yprobe = probe(coord.y, cmd.move.y);
        int zprobe = probe(coord.z, cmd.move.z);

        int collision = collisionXZ(ship, cx, cy, cz, xprobe, yprobe, zprobe);
        if((collision & 1) != 0) {
            cmd.move.setX(0.0f);
        }
        if((collision & 2) != 0) {
            cmd.move.setZ(0.0f);
        }
        pos.move(cmd.move);
    }

    private void updatePlayerRotation(int player, UserCommand cmd) {
        Rotation rot = (Rotation) world.getComponent(player, CompType.Rotation);
        rot.set(cmd.rot);
    }

    private void updateShip(int ship, UserCommand cmd) {
        Rotation shipRotation = (Rotation) world.getComponent(ship, CompType.Rotation);
        Quaternion shipRotationQuaternion = shipRotation.get();

        Vector3f vel = shipRotation.rotateLocal(new Vector3f(cmd.ship.left, cmd.ship.up, cmd.ship.forward));

        vel.multLocal(4.0f);

        Position shipPos = (Position) world.getComponent(ship, CompType.Position);
        shipPos.move(vel);

        // rotate rotation axis by fixedEntity rotation
        // this code is crazy - does it even work?
        // luckily this is only for debugging
        Vector3f yAxis = shipRotationQuaternion.multLocal(new Vector3f(0, 1, 0));
        Quaternion yRotation = new Quaternion().fromAngleAxis(cmd.ship.yaw, yAxis);

        Vector3f zAxis = shipRotationQuaternion.multLocal(new Vector3f(0, 0, 1));
        Quaternion zRotation = new Quaternion().fromAngleAxis(cmd.ship.roll, zAxis);

        Vector3f xAxis = shipRotationQuaternion.multLocal(new Vector3f(1, 0, 0));
        Quaternion xRotation = new Quaternion().fromAngleAxis(cmd.ship.pitch, xAxis);

        shipRotationQuaternion = yRotation.multLocal(shipRotationQuaternion).normalizeLocal();
        shipRotationQuaternion = zRotation.multLocal(shipRotationQuaternion).normalizeLocal();
        shipRotationQuaternion = xRotation.multLocal(shipRotationQuaternion).normalizeLocal();

        shipRotation.set(shipRotationQuaternion);
        shipRotation.setDirty(true);
    }

    private void changeFloppy(PlayerConnection connection, ChangeFloppy change) {
        InterfaceFloppyDrive vfd = (InterfaceFloppyDrive) world.getComponent(connection.item, CompType.VirtualFloppyDrive);
        if(vfd != null) {
            DCPU dcpu = (DCPU) world.getComponent(vfd.dcpuEID, CompType.DCPU);
            vfd.eject(dcpu);
            vfd.insert(dcpu, change.filename);
            unpickItem(connection, null);
        }
    }

    private void changeBootRom(PlayerConnection connection, ChangeBootRom change) {
        InterfaceFloppyDrive vfd = (InterfaceFloppyDrive) world.getComponent(connection.item, CompType.VirtualFloppyDrive);
        if(vfd != null) {
            DCPU dcpu = (DCPU) world.getComponent(vfd.dcpuEID, CompType.DCPU);
            dcpu.reset(change.filename);
            unpickItem(connection, null);
        }
    }

    private void build(PlayerConnection connection, Build build, Ship ship, BlockChanges changes) {
        Vector3Int vec = new Vector3Int(build.x, build.y, build.z);
        if(ship.getBlock(build.x, build.y, build.z) != 0) {
            Log.warn("Trying to build in non-empty block");
            return;
        }

        if(!isBlockDistanceBetween(connection.player, build.x, build.y, build.z, 1.1f, 3.0f)) {
            Log.warn("Trying to build too near or far");
            return;
        }

        ship.setBlock(vec, '#');
        changes.changes.add(new BlockChange(build.x, build.y, build.z, '#', BlockChange.Event.BUILD));
    }

    private void unbuild(PlayerConnection connection, Unbuild unbuild, Ship ship, BlockChanges changes) {
        Vector3Int vec = new Vector3Int(unbuild.x, unbuild.y, unbuild.z);
        if(unbuild.x < 0 || unbuild.y < 0 || unbuild.z < 0) {
            return;
        }

        if(ship.getBlock(unbuild.x, unbuild.y, unbuild.z) != '#') {
            return;
        }

        if(!isBlockDistanceBetween(connection.player, unbuild.x, unbuild.y, unbuild.z, 1.1f, 3.0f)) {
            return;
        }

        ship.setBlock(vec, (char) 0);
        changes.changes.add(new BlockChange(unbuild.x, unbuild.y, unbuild.z, (char) 0, BlockChange.Event.UNBUILD));
    }

    private void teleport(PlayerConnection connection, UserCommand.Teleport teleport) {
        // bind player to ship
        Log.mark();
        BindTo bind = (BindTo) world.getComponent(connection.player, CompType.BindTo);
        bind.setParent(teleport.eid);

        SpawnPoint sp = (SpawnPoint) world.getComponent(teleport.eid, CompType.SpawnPoint);

        Position pos = (Position) world.getComponent(connection.player, CompType.Position);
        pos.set(sp.vector);
    }

    private void processNewConnections(Connection[] connections) {
        for(Connection c: connections) {
            PlayerConnection pc = (PlayerConnection) c;
            if(!pc.isInitialized) {
                pc.isInitialized = true;
                replicateEntitiesToNewConnection(pc);
                initConnection(pc);
            }
        }
    }

    private void replicateEntitiesToNewConnection(PlayerConnection connection) {
        for(int eid = replicated.iterator(); eid != 0; eid = replicated.next()) {
            Log.debug(ID.get(eid));
            replicateComponents(connection, eid);
        }
    }

    private void replicateComponents(PlayerConnection connection, int eid) {
        Bag<Message> list = new Bag<>(20);
        list.add(new TimestampMessage());

        for(BaseComponent comp = world.compIter(eid); comp != null; comp=world.compNext()) {
            if(CompType.isReplicable(world.compIterPos)) {
                Log.debug(comp.toString());
                Message msg = comp.synchronize(eid);
                list.add(msg);
            }
        }

        if(list.size() > 0) {
            connection.sendTCP(list.toArray(Message.class));
        }
    }

    public void processDeletedEntities(Bag<Message> update) {
        for (int eid = deleted.iterator(); eid != 0; eid = deleted.next()) {
            Log.info(ID.get(eid));
            Flag flag = (Flag) world.getComponent(eid, CompType.FlagDelete);
            update.add(flag.synchronize(eid, CompType.FlagDelete));
            world.deleteEntity(eid);
        }
    }

    public void processSynchronizedEntities(Bag<Message> update) {
        for (int eid = group.iterator(); eid != 0; eid = group.next()) {
            for(BaseComponent comp = world.compIter(eid); comp != null; comp = world.compNext()) {
                if(comp.dirty && CompType.isReplicable(world.compIterPos)) {
                    comp.dirty = false;
                    Message msg = comp.synchronize(eid, world.compIterPos);
                    update.add(msg);
                }
            }
        }
    }

    private boolean isBlockDistanceBetween(int eid, int x, int y, int z, float min, float max) {
        Position pos = (Position) World.INSTANCE.getComponent(eid, CompType.Position);
        Vector3f buildPosition = new Vector3f(x + 0.5f, y + 0.5f, z + 0.5f);

        Log.debug("Builder position: " + pos.toString());
        Log.debug("Build position: " + buildPosition.toString());

        float distance = pos.get().distance(buildPosition);

        return distance > min && distance < max;
    }

    private void cmdText(PlayerConnection connection, String cmdText) {
        Log.info(cmdText);
        World.INSTANCE.setComponent(connection.player, CompType.CmdText, CmdText.create(cmdText));
    }

    private class NetworkListener extends Listener {
        @Override
        public void received(Connection connection, Object message) {
            try {
                ECSNetworkConnection clientConnection = (ECSNetworkConnection) connection;

                if(message instanceof ECSMessageLogin) {
                    clientConnection.nick = ((ECSMessageLogin) message).nick;

                } else if(o instanceof Network.Logout) {
                    handleLogoutMessage(pc, (Network.Logout) o);

                } else if(o instanceof UserCommand) {
                    handleUserCmd(pc, (UserCommand) o);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
