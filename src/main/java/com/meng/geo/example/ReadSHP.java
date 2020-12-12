package com.meng.geo.example;

import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class ReadSHP {

    public static void main(String[] args) {

        ReadSHP readSHP = new ReadSHP();

        String filePath = "/Users/dujianwei/every-x/geo-test/test.dbf";

        readSHP.readDBF(filePath);
    }

    private void readDBF(String filePath) {

        try (FileChannel fileChannel = new FileInputStream(filePath).getChannel();
             DbaseFileReader reader = new DbaseFileReader(fileChannel, false, Charset.forName("GBK"))) {

            DbaseFileHeader header = reader.getHeader();
            // 字段索引
            int numFields = header.getNumFields();
            // 遍历字段
            for (int i = 0; i < numFields; i++) {
                String fieldName = header.getFieldName(i);
                System.out.println("字段名称 : " + fieldName);
            }

            System.out.println();

            //迭代读取记录
            while (reader.hasNext()) {

                DbaseFileReader.Row row = reader.readRow();

                for (int i = 0; i < numFields; i++) {

                    String fieldName = header.getFieldName(i);
                    Object data = row.read(i);

                    System.out.println("字段名称 : " + fieldName);
                    System.out.println("字段的值 : " + data);
                }
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
