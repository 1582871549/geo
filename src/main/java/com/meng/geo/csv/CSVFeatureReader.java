package com.meng.geo.csv;

import com.csvreader.CsvReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.io.IOException;
import java.util.NoSuchElementException;

public class CSVFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    /** State used when reading file */
    protected ContentState state;

    /**
     * Current row number - used in the generation of FeatureId. TODO: Subclass ContentState to
     * track row
     */
    private int row;

    protected CsvReader reader;

    /** Utility class used to build features */
    protected SimpleFeatureBuilder builder;

    /** Factory class for geometry creation */
    private GeometryFactory geometryFactory;

    public CSVFeatureReader(ContentState contentState, Query query) throws IOException {
        this.state = contentState;
        CSVDataStore csv = (CSVDataStore) contentState.getEntry().getDataStore();
        reader = csv.read(); // this may throw an IOException if it could not connect
        boolean header = reader.readHeaders();
        if (!header) {
            throw new IOException("Unable to read csv header");
        }
        builder = new SimpleFeatureBuilder(state.getFeatureType());
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        row = 0;
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return state.getFeatureType();
    }

    /** The next feature */
    private SimpleFeature next;

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException, NoSuchElementException {
        SimpleFeature feature;
        if (next != null) {
            feature = next;
            next = null;
        } else {
            feature = readFeature();
        }
        return feature;
    }

    @Override
    public boolean hasNext() throws IOException {
        if (next != null) {
            return true;
        } else {
            next = readFeature(); // read next feature so we can check
            return next != null;
        }
    }

    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
            reader = null;
        }
        builder = null;
        geometryFactory = null;
        next = null;
    }

    /** Read a line of content from CSVReader and parse into values */
    private SimpleFeature readFeature() throws IOException {
        if (reader == null) {
            throw new IOException("FeatureReader is closed; no additional features can be read");
        }
        boolean read = reader.readRecord(); // read the "next" record
        if (!read) {
            close(); // automatic close to be nice
            return null; // no additional features are available
        }
        Coordinate coordinate = new Coordinate();
        for (String column : reader.getHeaders()) {
            String value = reader.get(column);
            if ("lat".equalsIgnoreCase(column)) {
                coordinate.y = Double.parseDouble(value.trim());
            } else if ("lon".equalsIgnoreCase(column)) {
                coordinate.x = Double.parseDouble(value.trim());
            } else {
                builder.set(column, value);
            }
        }
        builder.set("Location", geometryFactory.createPoint(coordinate));

        return this.buildFeature();
    }

    /** Build feature using the current row number to generate FeatureId */
    private SimpleFeature buildFeature() {
        row += 1;
        return builder.buildFeature(state.getEntry().getTypeName() + "." + row);
    }
}
