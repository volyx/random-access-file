package io.github.volyx;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class RecordsFileTest {

    private BaseRecordsFile recordsFile;

    @Before
    public void before() throws IOException {
        try {
            recordsFile = new BaseRecordsFile("testDatabase.jdb", "rw");
        } catch (IOException e) {
            recordsFile = new BaseRecordsFile("testDatabase.jdb", 64);
        }
    }

    @Test
    public void test1() throws IOException, ClassNotFoundException {
        int n = 5;
        for (int i = 0; i < n; i++) {
            final String key = "superDir" + i;
            String data = "Hello world!Hello world!Hello world!Hello world!Hello world!Hello world!" + i;
            Entry entry = new Entry(key, false, data.getBytes());
            recordsFile.insertRecord(entry);
            Entry readEntry = recordsFile.readRecord(key);

            System.out.println("Result: " + new String(entry.getData()) + " length = " + data.length());
        }



    }
}