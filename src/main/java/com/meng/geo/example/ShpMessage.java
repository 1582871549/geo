package com.meng.geo.example;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.GeometryAttribute;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Shapefile 文件扩展名
 * .shp 存储特征几何的主文件。必选
 * .shx 存储特征几何索引的索引文件。必选
 * .dbf 数据库——存储要素属性信息的数据库表。必选。几何和属性之间存在一一对应的关系，这种关系基于记录编号
 * .prj 存储坐标系信息的文件。由ArcGIS使用
 */
public class ShpMessage {

    public static void main(String[] args) throws Exception {

        ShpMessage message = new ShpMessage();

        String filePath = "/Users/dujianwei/every-x/geo-test/test.shp";
        // String filePath = "/Users/dujianwei/Downloads/郑州建筑轮廓/新建下载1建筑轮廓B_90045_57452_17.shp";

        message.aa(filePath);
    }

    private void aa(String filePath) throws IOException {

        // 1、获取shp文件
        File shapeFile = readShapeFile(filePath);
        // 2、获取数据存储
        DataStore dataStore = getDataStore(shapeFile);
        // 3、获取特征源
        getFeatureSource(dataStore);

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

    /**
     * 获取特征源
     *
     * Feature (负责封装空间几何要素对象)，对应于地图中一个实体
     *      包含:
     *          空间数据(Geometry)、
     *          属性数据(Aitribute)、
     *          参考坐标系(Refereneedsystem)、
     *          最小外包矩形(EnveloPe)等属性，是GIS操作的核心数据模型
     *
     * DBF文件中的数据类型FieldType
     *      代码    数据类型          允许输入的数据
     *      B      二进制型             各种字符。
     *      C      字符型               各种字符。
     *      D      日期型               用于区分年、月、日的数字和一个字符，内部存储按照YYYYMMDD格式。
     *      G   (Generalor OLE)        各种字符。
     *      N      数值型(Numeric)      - . 0 1 2 3 4 5 6 7 8 9
     *      L      逻辑型（Logical）     ? Y y N n T t F f (? 表示没有初始化)。
     *      M      (Memo)              各种字符。
     */
    private void getFeatureSource(DataStore dataStore) throws IOException {
        // 图层名称集合
        String[] typeNames = dataStore.getTypeNames();

        for (String typeName : typeNames) {

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

            while (featureIterator.hasNext()) {
                // 特征
                SimpleFeature feature = featureIterator.next();
                // 特征 -> 属性
                Collection<Property> properties = feature.getProperties();
                // 遍历属性
                for (Property property : properties) {
                    // 属性名称
                    String name = property.getName().toString();
                    // 属性值
                    Object value = property.getValue();
                    // 属性类型
                    String type = property.getType().toString();

                    System.out.println("属性名称 : " + name);
                    System.out.println("属性的值 : " + value);
                    // System.out.println("属性类型 : " + type);

                    if (value instanceof Point) {
                        Point point = (Point) value;
                        System.out.println("x : " + point.getX());
                        System.out.println("y : " + point.getY());
                    }

                    System.out.println();
                }

                // 要素类型、坐标
                Object geometryText = feature.getDefaultGeometry();

                // geometry属性
                GeometryAttribute geometryAttribute = feature.getDefaultGeometryProperty();
                // 获取坐标参考系信息
                CoordinateReferenceSystem coordinateReferenceSystem = geometryAttribute.getDescriptor().getCoordinateReferenceSystem();
                // geometry类型
                GeometryType geometryType = geometryAttribute.getType();
                // geometry类型名称
                Name name = geometryType.getName();

                System.out.println("要素geometry位置信息：" + geometryText);
                System.out.println("要素geometry类型名称：" + name);
                System.out.println("shp文件使用的坐标参考系：\n" + coordinateReferenceSystem);
                System.out.println();
            }

            // 关闭读取器
            featureIterator.close();
        }

    }

}
