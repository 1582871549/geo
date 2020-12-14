package com.meng.geo.csv;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.KVP;

import java.awt.RenderingHints;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class CSVDataStoreFactory implements DataStoreFactorySpi {

    /** Parameter description of information required to connect */
    public static final Param FILE_PARAM =
            new Param(
                    "file",
                    File.class,
                    "Comma seperated value file",
                    true,
                    null,
                    new KVP(Param.EXT, "csv"));

    /** Confirm DataStore availability, null if unknown */
    Boolean isAvailable = null;

    /**
     * Public "no argument" constructor called by Factory Service Provider (SPI) entry listed in
     * META-INF/services/org.geotools.data.DataStoreFactorySPI
     */
    public CSVDataStoreFactory() {}

    /** No implementation hints required at this time */
    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }

    @Override
    public DataStore createDataStore(Map<String, ?> params) throws IOException {
        File file = (File) FILE_PARAM.lookUp(params);
        return new CSVDataStore(file);
    }

    @Override
    public String getDisplayName() {
        return "CSV";
    }

    @Override
    public String getDescription() {
        return "Comma delimited text file.";
    }

    @Override
    public Param[] getParametersInfo() {
        return new Param[] {FILE_PARAM};
    }



    @Override
    public synchronized boolean isAvailable() {
        if (isAvailable == null) {
            try {
                Class cvsReaderType = Class.forName("com.csvreader.CsvReader");
                isAvailable = true;
            } catch (ClassNotFoundException e) {
                isAvailable = false;
            }
        }
        return isAvailable;
    }

    @Override
    public DataStore createNewDataStore(Map<String, ?> map) throws IOException {
        throw new UnsupportedOperationException("CSV Datastore is read only");
    }

    /**
     * Works for csv file.
     *
     * @param params connection parameters
     * @return true for connection parameters indicating a csv file
     */
    public boolean canProcess(Map<String, ?> params) {
        try {
            File file = (File) FILE_PARAM.lookUp(params);
            if (file != null) {
                return file.getPath().toLowerCase().endsWith(".csv");
            }
        } catch (IOException e) {
            // ignore as we are expected to return true or false
        }
        return false;
    }
}
