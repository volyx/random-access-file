package io.github.volyx;


public class Entry {

    private final String name;
    private final boolean isDir;
    private final byte[] data;

    public Entry(String name, boolean isDir, byte[] data) {
        this.name = name;
        this.isDir = isDir;
        this.data = data;
    }

    public Entry(String name, boolean isDir) {
        this.name = name;
        this.isDir = isDir;
        this.data = new byte[0];
    }

    public String getName() {
        return name;
    }

    public boolean isDir() {
        return isDir;
    }

    public byte[] getData() {
        return data;
    }
}
