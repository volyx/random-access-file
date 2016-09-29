package io.github.volyx;

import java.io.*;

import static io.github.volyx.BaseRecordsFile.DATA_ENTRY_LENGTH;

public class RecordHeader {
    /**
     * name of file
     */
    protected String name;
    /**
     * Actual number of bytes of data held in this record (4 bytes).
     */
    protected int dataCount;
    /**
     * Indicates this header's position in the file index. (4 bytes)
     */
    protected int indexPosition;
    /**
     * Indicates that recordHeader is directory header (~4 bytes)
     */
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
    protected RecordHeader(String name, boolean isDir, long dataPointer, int dataLength) {
        this.name = name;
        this.isDir = isDir;
        if (isDir) {
            this.childs = new long[] { dataPointer};

        } else {
            this.childCount = dataLength / DATA_ENTRY_LENGTH + 1;
            this.childs = new long[childCount];
            for (int i = 0; i < childCount; i++) {
                childs[i] = dataPointer + DATA_ENTRY_LENGTH * i;
            }
        }
        this.dataCount = 0;
    }
    protected int getIndexPosition() {
        return indexPosition;
    }
    protected void setIndexPosition(int indexPosition) {
        this.indexPosition = indexPosition;
    }

    protected void read(DataInput in) throws IOException {
        name = in.readUTF();
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
        out.writeInt(dataCount);
        out.writeBoolean(isDir);
        out.writeInt(childCount);
        for(int i = 0; i < childCount; i++) {
           out.writeLong(childs[i]);
        }

    }
}
