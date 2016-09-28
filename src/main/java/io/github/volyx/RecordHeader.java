package io.github.volyx;

import java.io.*;
public class RecordHeader {
    /**
     * name of file
     */
    protected String name;
    /**
     * File pointer to the first byte of record data (8 bytes).
     */
    protected long dataPointer;
    /**
     * Actual number of bytes of data held in this record (4 bytes).
     */
    protected int dataCount;
    /**
     * Number of bytes of data that this record can hold (4 bytes).
     */
    protected int dataCapacity;
    /**
     * Indicates this header's position in the file index. (4 bytes)
     */
    protected int indexPosition;

    protected boolean isDir;
    /**
     * Number of child (4 bytes)
     */
    protected int childCount;
    /**
     * Arrays of child. childCount * 8 bytes
     */
    protected long[] childs;

    protected RecordHeader() {
    }
    protected RecordHeader(String name, boolean isDir, long dataPointer, int dataCapacity) {
        this.name = name;
        this.isDir = isDir;
        if (dataCapacity < 1) {
            throw new IllegalArgumentException("Bad record size: " + dataCapacity);
        }
        this.dataPointer = dataPointer;
        this.dataCapacity = dataCapacity;
        this.dataCount = 0;
    }
    protected int getIndexPosition() {
        return indexPosition;
    }
    protected void setIndexPosition(int indexPosition) {
        this.indexPosition = indexPosition;
    }
    protected int getDataCapacity() {
        return dataCapacity;
    }
    protected int getFreeSpace() {
        return dataCapacity - dataCount;
    }
    protected void read(DataInput in) throws IOException {
        name = in.readUTF();
        dataPointer = in.readLong();
        dataCapacity = in.readInt();
        dataCount = in.readInt();
        isDir = in.readBoolean();
        childCount = in.readInt();
        childs = new long[childCount];
        for(int i = 0; i < childCount; i++) {
            childs[i] = in.readLong();
        }
    }
    protected void write(DataOutput out) throws IOException {
        out.writeUTF(name);
        out.writeLong(dataPointer);
        out.writeInt(dataCapacity);
        out.writeInt(dataCount);
        out.writeBoolean(isDir);
        out.writeInt(childCount);
        for(int i = 0; i < childCount; i++) {
           out.writeLong(childs[i]);
        }

    }

    /**
     * Returns a new record header which occupies the free space of this record.
     * Shrinks this record size by the size of its free space.
     */
    protected RecordHeader split() throws IOException {
        long newFp = dataPointer + (long)dataCount;
        RecordHeader newRecord = new RecordHeader(name, isDir, newFp, getFreeSpace());
        dataCapacity = dataCount;
        return newRecord;
    }
}
