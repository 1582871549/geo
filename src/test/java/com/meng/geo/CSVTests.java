package com.meng.geo;

import com.csvreader.CsvReader;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

@SpringBootTest
class CSVTests {

    @Test
    void contextLoads() throws IOException {

        String path = "/Users/dujianwei/upload/csv/locations.csv";
        File file = new File(path);

        List<String> cities = new ArrayList<>();

        try (FileReader reader = new FileReader(file)) {
            CsvReader locations = new CsvReader(reader);
            locations.readHeaders();
            while (locations.readRecord()) {
                cities.add(locations.get("CITY"));
            }
        }
        assertTrue(cities.contains("Victoria"));
    }

}
