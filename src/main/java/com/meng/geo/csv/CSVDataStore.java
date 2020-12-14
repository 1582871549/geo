package com.meng.geo.csv;

import com.csvreader.CsvReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentDataStore;
import org.geotools.data.store.ContentEntry;
import org.geotools.data.store.ContentFeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

public class CSVDataStore extends ContentDataStore {

    File file;

    public CSVDataStore(File file) {
        this.file = file;
    }



    /**
     * 一个数据存储可以提供对几种不同数据产品的访问。方法createTypeNames提供了正在发布的信息的列表。
     *
     * @return 仅包含一个csv文件的名称
     */
    @Override
    protected List<Name> createTypeNames() {
        String name = file.getName();
        name = name.substring(0, name.lastIndexOf('.'));

        Name typeName = new NameImpl(name);
        return Collections.singletonList(typeName);
    }

    @Override
    protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
        return new CSVFeatureSource(entry, Query.ALL);
    }


    /**
     * 允许对文件的读访问、完成后请关闭阅读器。
     *
     * @return CsvReader for file
     */
    CsvReader read() throws IOException {
        Reader reader = new FileReader(file);
        CsvReader csvReader = new CsvReader(reader);
        return csvReader;
    }
}
