package org.megastage.server;

public class MessageLEM {
    public char videoAddr = 0x8000;
    public RAMArea video = new RAMArea(LEMUtil.defaultVideo);

    public char fontAddr = 0x0000;
    public RAMArea font = new RAMArea(LEMUtil.defaultFont);

    public char paletteAddr = 0x0000;
    public RAMArea palette = new RAMArea(LEMUtil.defaultPalette);

    /*
    public void receive(int eid) {
        //ClientVideoMemory videoMemory = World.INSTANCE.getOrCreateComponent(eid, CompType.ClientVideoMemory, ClientVideoMemory.class);
        //videoMemory.update(this);
    }
    */
}
