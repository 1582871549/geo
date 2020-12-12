package com.meng.geo.shape.utils;

import com.meng.geo.shape.entity.GISBase;
import com.meng.geo.shape.entity.GISLine;
import com.meng.geo.shape.entity.GISMultiPolygon;
import com.meng.geo.shape.entity.GISPoint;
import com.meng.geo.shape.entity.ShapeProperty;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shape File
 *      文件用于描述空间数据中的矢量数据。
 *      例如几何体对象：点，折线与多边形。以及同时存储几何形状与属性信息，这是Shapefile文件最基本的特征。
 *
 * 主要有三个主文件，及若干子文件组成的名称相同的文件。
 *      1 .shp 用于保存元素的几何实体的，即主文件
 *      2 .shx 图形索引格式 几何体位置索引，记录每一个几何体在shp文件之中的位置，能够加快向前或向后搜索一个几何体的效率。
 *      3 .dbf 属性数据格式，以dBase IV的数据表格式存储每个几何形状的属性数据
 */
public class ShapeUtils {

    public static void main(String[] args) throws IOException {

        String path = "/Users/dujianwei/Downloads/新建下载4百度POI_45131_28756_16/新建下载4百度POI_45131_28756_16.shp";

        ShapeUtils utils = new ShapeUtils();

        List<ShapeProperty> infos = utils.getShapeInfo(path);

        for (ShapeProperty info : infos) {
            System.out.println(info);
        }
    }

    private List<ShapeProperty> getShapeInfo(String filePath) throws IOException {

        File shapeFile = readShapeFile(filePath);

        DataStore dataStore = getDataStore(shapeFile);

        return getPropertys(dataStore);
    }

    private List<ShapeProperty> getPropertys(DataStore dataStore) throws IOException {
        // 图层名称
        String typeName = dataStore.getTypeNames()[0];
        // 特征源
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        SimpleFeatureType schema = featureSource.getSchema();

        List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();

        List<ShapeProperty> properties = new ArrayList<>();

        for (AttributeDescriptor attr : attributes) {

            ShapeProperty field = new ShapeProperty();
            field.setName(attr.getLocalName());
            field.setType(attr.getType().getBinding());

            properties.add(field);
        }
        return properties;
    }

    public List<GISBase> readGisObject(String filePath) throws IOException {

        File shapeFile = readShapeFile(filePath);

        DataStore dataStore = getDataStore(shapeFile);

        List<GISBase> geoms = getFeatureSource(dataStore);

        createGisMap(geoms);

        return geoms;
    }

    /**
     * 获取shp文件
     */
    private File readShapeFile(String filePath) {

        File file = new File(filePath);

        if (file.exists()) {
            return file;
        }
        throw new RuntimeException("文件不存在");
    }

    /**
     * 获取数据存储
     */
    private DataStore getDataStore(File shapeFile) throws IOException {
        // 读取shp文件到数据存储中
        Map<String, Object> params = new HashMap<>();
        params.put("url", shapeFile.toURI().toURL());
        params.put("create spatial index", false);
        params.put("memory mapped buffer", false);
        params.put("charset", "GBK");

        return DataStoreFinder.getDataStore(params);
    }

    private List<GISBase> getFeatureSource(DataStore dataStore) throws IOException {

        // 图层名称
        String typeName = dataStore.getTypeNames()[0];
        // 特征源
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        // 特征集合
        SimpleFeatureCollection features = featureSource.getFeatures();
        // 特征数量
        int size = features.size();
        // 特征迭代器
        SimpleFeatureIterator featureIterator = features.features();

        System.out.println("图层名称 : " + typeName);
        System.out.println("特征数量 : " + size);
        System.out.println();

        List<GISBase> geoms = new ArrayList<>();
        // List<ShapeProperty> properties = new ArrayList<>();

        while (featureIterator.hasNext()) {
            // 特征
            SimpleFeature feature = featureIterator.next();
            // 要素类型、坐标
            Object geometry = feature.getDefaultGeometry();
            GISBase gisBase = null;

            if (geometry instanceof Point) {
                gisBase = new GISPoint((Point) geometry, feature);
            }
            if (geometry instanceof MultiLineString) {
                gisBase = new GISLine((MultiLineString) geometry, feature);
            }
            if (geometry instanceof MultiPolygon) {
                gisBase = new GISMultiPolygon((MultiPolygon) geometry, feature);
            }

            if (gisBase != null) {
                // 特征 -> 属性
                Collection<Property> properties = feature.getProperties();
                List<ShapeProperty> propertieList = gisBase.getPropertieList();

                for (Property property : properties) {
                    // 属性名称
                    String name = property.getName().toString();
                    // 属性类型
                    Class<?> type = property.getType().getBinding();

                    propertieList.add(new ShapeProperty(name, type));
                }
                geoms.add(gisBase);
            }
        }
        featureIterator.close();

        return geoms;
    }

    private void createGisMap(List<GISBase> geoms) {

        for (GISBase base : geoms) {

            List<ShapeProperty> propertieList = base.getPropertieList();
            Map<String, Object> propertyMap = base.getPropertyMap();

            for (ShapeProperty property : propertieList) {

                String propertyName = property.getName();
                SimpleFeature feature = base.getSimpleFeature();
                Object value = feature.getAttribute(propertyName);

                propertyMap.put(propertyName, value);
            }
        }
    }

}
