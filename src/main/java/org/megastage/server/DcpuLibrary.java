package org.megastage.server;

import org.megastage.ecs.Singleton;

import java.io.*;
import java.util.HashMap;

@Singleton
public class DcpuLibrary {
    public final HashMap<String, DcpuMedia> bootroms = new HashMap<>();
    public final HashMap<String, DcpuMedia> floppies = new HashMap<>();

    public DcpuLibrary() {
        File folder = new File("media");
        for(File f: folder.listFiles()) {
            if(f.isFile()) {
                if(f.getName().endsWith(".bin")) {
                    bootroms.put(f.getName(), DcpuMedia.load(DcpuMedia.MediaType.Bootrom, f));
                } else if(f.getName().endsWith(".d16")) {
                    floppies.put(f.getName(), DcpuMedia.load(DcpuMedia.MediaType.Floppy, f));
                }
            }
        }
    }

    public String[] getBootromNames() {
        return bootroms.keySet().toArray(new String[bootroms.size()]);
    }

    public String[] getFloppyNames() {
        return floppies.keySet().toArray(new String[floppies.size()]);
    }
}
