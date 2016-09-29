package io.github.volyx;

import java.io.*;

import java.util.*;

public class BaseRecordsFile {
    // The database file.
    private RandomAccessFile file;
    // Current file pointer to the start of the record data.
    private long dataStartPtr;
    // Total length in bytes of the global database headers.
    protected static final int FILE_HEADERS_REGION_LENGTH = 16;
    // Number of bytes in the record header.
    protected static final int INDEX_ENTRY_LENGTH = 64;
    // Number of bytes in the data block
    protected static final int DATA_ENTRY_LENGTH = 64;
    // File pointer to the num records header.
    protected static final long NUM_RECORDS_HEADER_LOCATION = 0;
    // File pointer to the data start pointer header.
    protected static final long DATA_START_HEADER_LOCATION = 4;

    protected static final int ROOT_POSITION = 0;
    /**
     * Creates a new database file, initializing the appropriate headers. Enough space is allocated in
     * the index for the specified initial size.
     */
    protected BaseRecordsFile(String dbPath, int initialSize) throws IOException {
        File f = new File(dbPath);
        if (f.exists()) {
            throw new IOException("Database already exits: " + dbPath);
        }
        file = new RandomAccessFile(f, "rw");
        dataStartPtr = indexPositionToRecordHeaderFp(initialSize);  // Record Data Region starts were the
        setFileLength(dataStartPtr);                       // (i+1)th index entry would start.
        writeNumRecordsHeader(0);
        writeDataStartPtrHeader(dataStartPtr);

        insertRecord(new Entry("/", true));
    }

    private void writeRootHeader(long dataStartPtr) {

    }

    /**
     * Opens an existing database file and initializes the dataStartPtr. The accessFlags
     * parameter can be "r" or "rw" -- as defined in RandomAccessFile.
     */
    protected BaseRecordsFile(String dbPath, String accessFlags) throws IOException {
        File f = new File (dbPath);
        if(!f.exists()) {
            throw new IOException("Database not found: " + dbPath);
        }
        file = new RandomAccessFile(f, accessFlags);
        dataStartPtr = readDataStartHeader();
    }

    protected long getFileLength() throws IOException {
        return file.length();
    }
    protected void setFileLength(long l) throws IOException {
        file.setLength(l);
    }
    /**
     * Reads the number of records header from the file.
     */
    protected int readNumRecordsHeader() throws IOException {
        file.seek(NUM_RECORDS_HEADER_LOCATION);
        return file.readInt();
    }
    /**
     * Writes the number of records header to the file.
     */
    protected void writeNumRecordsHeader(int numRecords) throws IOException {
        file.seek(NUM_RECORDS_HEADER_LOCATION);
        file.writeInt(numRecords);
    }
    /**
     * Reads the data start pointer header from the file.
     */
    protected long readDataStartHeader() throws IOException {
        file.seek(DATA_START_HEADER_LOCATION);
        return file.readLong();
    }
    /**
     * Writes the data start pointer header to the file.
     */
    protected void writeDataStartPtrHeader(long dataStartPtr) throws IOException {
        file.seek(DATA_START_HEADER_LOCATION);
        file.writeLong(dataStartPtr);
    }
    /**
     * Returns a file pointer in the index pointing to the first byte
     * in the record pointer located at the given index position.
     */
    protected long indexPositionToRecordHeaderFp(int pos) {
        return FILE_HEADERS_REGION_LENGTH + (INDEX_ENTRY_LENGTH * pos);
    }

    /**
     * Reads the ith record header from the index.
     */
    RecordHeader readRecordHeaderFromIndex(int position) throws IOException {
        file.seek(indexPositionToRecordHeaderFp(position));
        RecordHeader r = new RecordHeader();
        r.read(file);
        return r;
    }
    /**
     * Writes the ith record header to the index.
     */
    protected void writeRecordHeaderToIndex(RecordHeader header) throws IOException {
        file.seek(indexPositionToRecordHeaderFp(header.indexPosition));
        header.write(file);
    }
    /**
     * Appends an entry to end of index. Assumes that insureIndexSpace() has already been called.
     */
    protected void addEntryToIndex(String key, RecordHeader newRecord, int currentNumRecords) throws IOException {
        file.seek(indexPositionToRecordHeaderFp(currentNumRecords));
        newRecord.write(file);
        newRecord.setIndexPosition(currentNumRecords);
        writeNumRecordsHeader(currentNumRecords+1);
    }
    /**
     * Removes the record from the index. Replaces the target with the entry at the
     * end of the index.
     */
    protected void deleteEntryFromIndex(String key, RecordHeader header, int currentNumRecords) throws IOException {
        if (header.indexPosition != currentNumRecords -1) {
            RecordHeader last = readRecordHeaderFromIndex(currentNumRecords-1);
            last.setIndexPosition(header.indexPosition);
            file.seek(indexPositionToRecordHeaderFp(last.indexPosition));
            last.write(file);
        }
        writeNumRecordsHeader(currentNumRecords-1);
    }
    /**
     * Adds the given record to the database.
     */
    public synchronized void insertRecord(Entry entry) throws IOException {
        if (recordExists(entry.getName())) {
            throw new IOException("Key exists: " + entry.getName());
        }
        insureIndexSpace(readNumRecordsHeader() + 1);
        RecordHeader newRecord = allocateRecord(entry.getName(), entry.isDir(), entry.getData().length);
        writeRecordData(newRecord, entry.getData());
        addEntryToIndex(entry.getName(), newRecord, readNumRecordsHeader());
    }

    private boolean recordExists(String name) throws IOException {
        PathParser p = new PathParser();
        RecordHeader header = p.parse(name);
        return header != null;
    }

    /**
     * Returns the record to which the target file pointer belongs - meaning the specified location
     * in the file is part of the record data of the RecordHeader which is returned.  Returns null if
     * the location is not part of a record. (O(n) mem accesses)
     */
    protected RecordHeader getRecordAt(long headerPointer) throws IOException {
        file.seek(headerPointer);
        RecordHeader r = new RecordHeader();
        r.read(file);
        return r;
    }

    /**
     * Updates an existing record. If the new contents do not fit in the original record,
     * then the update is handled by deleting the old record and adding the new.
     */
//    public synchronized void updateRecord(String key, boolean isDir, byte[] data) throws IOException {
//        RecordHeader header = keyToRecordHeader(key);
//        if (data.length > header.dataCapacity) {
//            deleteRecord(key);
//            insertRecord(key, isDir, data);
//        } else {
//            writeRecordData(header, data);
//            writeRecordHeaderToIndex(header);
//        }
//    }
    /**
     * Reads a record.
     */
    public synchronized Entry readRecord(String key) throws IOException {
        byte[] data = readRecordData(key);
        return new Entry(key, false, data);
    }
    /**
     * Reads the data for the record with the given key.
     */
    protected byte[] readRecordData(String key) throws IOException {
        return readRecordData(keyToRecordHeader(key));
    }

    private RecordHeader keyToRecordHeader(String key) {
        return null;
    }

    /**
     * Reads the record data for the given record header.
     */
    protected byte[] readRecordData(RecordHeader header) throws IOException {
        byte[] buf = new byte[header.dataCount];
        int from;
        int left;
        for (int i = 0; i < header.childCount; i++) {
            file.seek(header.childs[i]);
            from = i * DATA_ENTRY_LENGTH;
            left = header.dataCount - from;
            if (left < DATA_ENTRY_LENGTH) {
                file.readFully(buf, from, left);
            } else {
                file.readFully(buf, from, DATA_ENTRY_LENGTH);
            }

        }

        return buf;
    }
    /**
     * Updates the contents of the given record. A RecordsFileException is thrown if the new data does not
     * fit in the space allocated to the record. The header's data count is updated, but not
     * written to the file.
     */
    protected void writeRecordData(RecordHeader header, byte[] data) throws IOException {
//        if (data.length > header.dataCapacity) {
//            throw new IOException ("Record data does not fit");
//        }
        header.dataCount = data.length;
        int from;
        int left;
        for (int i = 0; i < header.childCount; i++) {
            file.seek(header.childs[i]);
            from = i * DATA_ENTRY_LENGTH;
            left = header.dataCount - from;
            if (left < DATA_ENTRY_LENGTH) {
                file.write(data, from, left);
            } else {
                file.write(data, from, DATA_ENTRY_LENGTH);
            }
        }


    }
    /**
     * Deletes a record.
     */
    public synchronized void deleteRecord(String key) throws IOException {
//        RecordHeader delRec = keyToRecordHeader(key);
//        int currentNumRecords = readNumRecordsHeader();
//        if (getFileLength() == delRec.dataPointer + delRec.dataCapacity) {
//            // shrink file since this is the last record in the file
//            setFileLength(delRec.dataPointer);
//        } else {
//            RecordHeader previous = getRecordAt(delRec.dataPointer -1);
//            if (previous != null) {
//
//                // append space of deleted record onto previous record
//
//                previous.dataCapacity += delRec.dataCapacity;
//
//                writeRecordHeaderToIndex(previous);
//            } else {
//
//                // target record is first in the file and is deleted by adding its space to
//
//                // the second record.
//
//                RecordHeader secondRecord = getRecordAt(delRec.dataPointer + (long)delRec.dataCapacity);
//
//                byte[] data = readRecordData(secondRecord);
//
//                secondRecord.dataPointer = delRec.dataPointer;
//
//                secondRecord.dataCapacity += delRec.dataCapacity;
//
//                writeRecordData(secondRecord, data);
//
//                writeRecordHeaderToIndex(secondRecord);
//            }
//        }
//        deleteEntryFromIndex(key, delRec, currentNumRecords);
    }
    // Checks to see if there is space for and additional index entry. If
    // not, space is created by moving records to the end of the file.
    protected void insureIndexSpace(int requiredNumRecords) throws IOException {
        int currentNumRecords = readNumRecordsHeader();
        long endIndexPtr = indexPositionToRecordHeaderFp(requiredNumRecords);
        if (endIndexPtr > getFileLength() && currentNumRecords == 0) {
            setFileLength(endIndexPtr);
            dataStartPtr = endIndexPtr;
            writeDataStartPtrHeader(dataStartPtr);
            return;
        }
//        while (endIndexPtr > dataStartPtr) {
//            RecordHeader first = getRecordAt(dataStartPtr);
//            byte[] data = readRecordData(first);
//            first.dataPointer = getFileLength();
//            first.dataCapacity = data.length;
//            setFileLength(first.dataPointer + data.length);
//            writeRecordData(first, data);
//            writeRecordHeaderToIndex(first);
//            dataStartPtr += first.dataCapacity;
//            writeDataStartPtrHeader(dataStartPtr);
//        }
    }
    /**
     * Closes the file.
     */
    public synchronized void close() throws IOException {
        try {
            file.close();
        } finally {
            file = null;
        }
    }

    /**
     * This method searches the file for free space and then returns a RecordHeader
     * which uses the space. (O(n) memory accesses)
     */
    protected RecordHeader allocateRecord(String key, boolean isDir, int dataLength) throws IOException {
        long fp = getFileLength();
        RecordHeader newRecord = new RecordHeader(key, isDir, fp, dataLength);
        setFileLength(fp + newRecord.childCount * DATA_ENTRY_LENGTH);
        return newRecord;
    }

    class PathParser {

        public RecordHeader parse(String path) throws IOException {
            String[] parts = path.split(File.separator);

            RecordHeader rootHeader = readRecordHeaderFromIndex(ROOT_POSITION);

            if (File.separator.equals(path)) {
                return rootHeader;
            }

            if (parts.length > 0) {
                for (int i = 0; i < parts.length; i++) {}

                String dir = parts[0];
                RecordHeader recordHeader = getChildRecordHeader(rootHeader, dir);
                if (recordHeader != null) return recordHeader;
            }

            return null;
        }

    }

    private RecordHeader getChildRecordHeader(RecordHeader rootHeader, String dir) throws IOException {
        for (int i = 0; i < rootHeader.childCount; i++) {
            RecordHeader recordHeader = getRecordAt(rootHeader.childs[i]);
            if (recordHeader.name.equals(dir)) {
                return recordHeader;
            }
        }
        return null;
    }

}