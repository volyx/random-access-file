package io.github.volyx;


public class Entry {

    String name;
    boolean isDir;
    byte[] data;

    public Entry(String name, byte[] data) {
        this.name = name;
        this.data = data;
    }
}
