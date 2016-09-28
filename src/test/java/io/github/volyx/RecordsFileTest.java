package io.github.volyx;


import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class RecordsFileTest {

    @Test
    public void test1() throws IOException, ClassNotFoundException {
        RecordsFile recordsFile = new RecordsFile("testDatabase.jdb", 64);
        RecordWriter rw = new RecordWriter("foo.lastAccessTime");
        Date date = new Date();
        rw.writeObject(date);
        recordsFile.insertRecord(rw);

        RecordReader rr = recordsFile.readRecord("foo.lastAccessTime");
        Date d = (Date)rr.readObject();

        Assert.assertEquals(date, d);
        System.out.println("last access was at: " + d.toString());
    }
}