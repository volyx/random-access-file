package io.github.volyx;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class RecordsFileTest {

    private RecordsFile recordsFile;

    @Before
    public void before() throws IOException {
        try {
            recordsFile = new RecordsFile("testDatabase.jdb", "rw");
        } catch (IOException e) {
            recordsFile = new RecordsFile("testDatabase.jdb", 64);
        }
    }

    @Test
    public void test1() throws IOException, ClassNotFoundException {

        final String key = "foo.lastAccessTime-" + UUID.randomUUID().toString();
        RecordWriter rw = new RecordWriter(key);
        Date date = new Date();
        rw.writeObject(date);
        recordsFile.insertRecord(rw);

        RecordReader rr = recordsFile.readRecord(key);
        Date d = (Date)rr.readObject();

        Assert.assertEquals(date, d);
        System.out.println("last access was at: " + d.toString());
    }
}