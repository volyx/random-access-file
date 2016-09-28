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

        final String key = "superDir" + UUID.randomUUID().toString();
        String data = "Hello world!";
        recordsFile.insertRecord(key, true, data.getBytes());

        Entry entry = recordsFile.readRecord(key);

        System.out.println("Result: " + new String(entry.data));
    }
}