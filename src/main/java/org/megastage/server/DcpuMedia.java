package org.megastage.server;

import com.esotericsoftware.minlog.Log;

import java.io.*;

public class DcpuMedia {

    public MediaType type;

    public char[] data;
    public boolean writeProtected;

    public DcpuMedia(MediaType type) {
        this.type = type;
        this.data = new char[type.capacity];
    }

    public void load(File file) {
        try(DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            for (int i = 0; i < data.length; i++) {
                data[i] = dis.readChar();
            }
        } catch (EOFException ignore) {
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    public void save(File file) {
        try(DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            for(char aData : data) {
                dos.writeChar(aData);
            }
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    public boolean isWriteProtected() {
        return writeProtected;
    }

    public void setWriteProtected(boolean writeProtected) {
        this.writeProtected = writeProtected;
    }

    public static DcpuMedia load(MediaType type, File file) {
        DcpuMedia media =  new DcpuMedia(type);
        media.load(file);
        return media;
    }

    public enum MediaType {
        Bootrom(65536), Floppy(737280);

        private final int capacity;

        MediaType(int capacity) {
            this.capacity = capacity;
        }
    }
}
