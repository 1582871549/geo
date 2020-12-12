package com.meng.geo.example;

import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import javax.swing.UIManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaCSV是一个开放源代码库，用于读取具有多种选项的CSV文件。
 *
 * 使用相同的技术从其他结构化文件格式（例如GeoJson）的数据创建shapefile
 *
 * Java   |  Geospatial
 * -------|-----------
 * Object |  Feature
 * Class  |  FeatureType
 * Field  |  Attribute
 * Method |  Operation
 */
public class Csv2Shape {

    static {
        // Set System L&F
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {

        Csv2Shape csv2Shape = new Csv2Shape();

        String csvFilePath = "/Users/dujianwei/upload/geo/locations.csv";

        csv2Shape.writeSHP(csvFilePath);
    }

    private void writeSHP(String csvFilePath) throws SchemaException, IOException {

        // 获取文件
        File file = readFile(csvFilePath);
        // 创建要素类型
        SimpleFeatureType type = createFeatureType();
        // 创建特征
        List<SimpleFeature> features = createFeatures(file, type);
        // 获取数据源
        ShapefileDataStore dataStore = getStore(file, type);
        // 将特征数据写入shapeFile
        saveData(type, features, dataStore);

    }

    /**
     * 获取文件
     */
    private File readFile(String filePath) {

        File file = new File(filePath);

        if (file.exists()) {
            return file;
        }
        throw new RuntimeException("文件不存在");
    }

    /**
     * 创建要素类型
     * 我们创建一个FeatureType描述从CSV文件导入并写入shapefile的数据。
     * 在这里，我们使用DataUtilities便捷类：
     */
    private SimpleFeatureType createFeatureType() throws SchemaException {
        // 几何属性:点类型 + 字符串属性 + 数字属性
        String typeSpec = "the_geom:Point:srid=4326," + "name:String," + "number:Integer";

        final SimpleFeatureType TYPE = DataUtilities.createType("Location", typeSpec);

        System.out.println("TYPE:" + TYPE);

        return TYPE;
    }

    /**
     * 请注意，使用大写常量保存SimpleFeatureType。
     * 由于SimpleFeatureType 该类是不可变的，因此将它们作为最终变量进行跟踪可以帮助您记住一旦创建便无法对其进行修改。
     *
     * 使用此方法，我们SimpleFeatureType包含一个，CoordinateReferenceSystem
     * 因此无需调用forceSchemaCRS即可生成.prj文件。
     * 另外，我们现在将“名称”字段限制为15个字符。
     */
    private SimpleFeatureType createFeatureType2() {

        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");
        builder.setCRS(DefaultGeographicCRS.WGS84); // <- Coordinate reference system 坐标参考系统（WGS84 = 世界经纬度）

        // 按顺序添加属性
        builder.add("the_geom", Point.class);
        builder.length(15).add("name", String.class); // <- 15 chars width for name field
        builder.add("number", Integer.class);

        // build the type
        final SimpleFeatureType LOCATION = builder.buildFeatureType();

        System.out.println("TYPE:" + LOCATION);

        return LOCATION;
    }


    /**
     * 特征
     *  概念：特征是可以在地图上绘制的东西。
     *  严格的定义是，特征是现实世界中的某物-景观特征-珠穆朗玛峰，埃菲尔铁塔，甚至是像您的大姨妈爱丽丝那样四处移动的东西。
     *
     *  向Java开发人员解释这个概念很容易 —— 特征就是对象。
     *  特征可以包含一些有关它们所代表的现实世界的信息。该信息被组织称为属性，就像在Java中将信息插入字段一样。
     *
     *  偶尔，你的俩个特征会有很多共同点。比如：洛杉矶的洛杉矶国际机场和悉尼的SYD机场
     *  因为这两个特征有几个共同点，所以将它们组合在一起很好
     *  在Java中，我们会创建一个名为 Airport（机场）的类。在地图上，我们将创建一个名为机场的特征类型（FeatureType）。
     *
     * 创建特征
     *  现在，我们可以读取CSV文件并为每条记录创建一个功能。请注意以下事项：
     *  使用的GeometryFactory创建新的点
     *  使用创建特征（SimpleFeature对象）SimpleFeatureBuilder
     */
    private List<SimpleFeature> createFeatures(File cvsFile, SimpleFeatureType type) throws IOException {

        // 收集特征
        List<SimpleFeature> features = new ArrayList<>();
        // 几何工厂将用于创建每个要素的几何属性，使用点对象作为位置。
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        // 特征构造器
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);

        try (BufferedReader reader = new BufferedReader(new FileReader(cvsFile))) {
            // 第一行是标题
            String line = reader.readLine();
            System.out.println("Header: " + line);

            for (line = reader.readLine(); line != null; line = reader.readLine()) {
                // skip blank lines
                if (line.trim().length() > 0) {

                    System.out.println("line: " + line);

                    String[] tokens = line.split(",");

                    double latitude = Double.parseDouble(tokens[0]);    // 纬度
                    double longitude = Double.parseDouble(tokens[1]);   // 经度
                    String name = tokens[2].trim();
                    int number = Integer.parseInt(tokens[3].trim());

                    /* 经度(= x坐标) */
                    Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

                    featureBuilder.add(point);
                    featureBuilder.add(name);
                    featureBuilder.add(number);

                    SimpleFeature feature = featureBuilder.buildFeature(null);
                    features.add(feature);
                }
            }
        }

        return features;
    }

    /**
     * 从FeatureCollection创建shapeFile
     * 创建shapeFile时要注意的事项：
     *      DataStoreFactory与参数一起使用表示我们想要空间索引
     *      使用createSchema(SimpleFeatureType)方法设置shapeFile（我们将getNewShapeFile在下一节中创建方法）
     */
    private ShapefileDataStore getStore(File cvsFile, SimpleFeatureType type) throws IOException {

        // 获取输出文件名并创建新的形状文件
        File shapeFile = getShapeFile(cvsFile);
        URL url = shapeFile.toURI().toURL();


        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", url);
        params.put("create spatial index", Boolean.TRUE);
        params.put("charset", "GBK");

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        // 类型用作描述文件内容的模板
        newDataStore.createSchema(type);

        return newDataStore;
    }

    /**
     * 提示用户输入输出形状文件的名称和路径
     *
     * @param csvFile 用于创建默认形状文件名的输入csv文件
     * @return name and path for the shapeFile as a new File object
     */
    private File getShapeFile(File csvFile) {
        String csvPath = csvFile.getAbsolutePath();
        String shpPath = csvPath.substring(0, csvPath.length() - 4) + ".shp";
        return new File(shpPath);
    }

    /**
     *
     * FeatureSource用于读取功能，子类FeatureStore是用于读/写访问。
     *
     * 判断File是否可以在GeoTools中写入的方法是使用instanceof检查。
     *
     * String typeNames = dataStore.getTypeNames()[0];
     * SimpleFeatureSource source = store.getfeatureSource(typeName);
     *
     * if (source instanceof SimpleFeatureStore) {
     *    SimpleFeatureStore store = (SimpleFeatureStore) source; // write access!
     *    store.addFeatures( featureCollection );
     *    store.removeFeatures( filter ); // filter is like SQL WHERE
     *    store.modifyFeature( attribute, value, filter );
     * }
     * 我们决定将写访问权限作为子类（而不是isWritable方法）进行处理，以使方法不受阻碍，除非可以使用它们。
     *
     *
     * 将特征数据写入shapefile
     * 注意事项：
     *      我们通过确认我们的对象实现了方法来检查我们是否具有读写访问权限FeatureSourceFeatureStore
     *      花一点时间检查一下shapefile与模板（SimpleFeatureType TYPE）的匹配程度。比较此输出以了解它们的不同之处。
     *      在SimpleFeatureStore我们使用要做到这一点需要一个FeatureCollection对象，所以我们总结我们的一个功能列表ListFeatureCollection。
     *      使用transaction.commit()安全地一次性写出功能。
     */
    private void saveData(SimpleFeatureType type, List<SimpleFeature> features, ShapefileDataStore dataStore) throws IOException {

        // 将特征写入形状文件
        Transaction transaction = new DefaultTransaction("create");

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();
        /*
         * 形状文件格式有几个限制:
         * - "the_geom" 总是第一个，用于几何属性名称
         * - "the_geom" 必须是点、线字符串、多边形类型
         * - 属性名长度有限
         * - 每个数据存储都有不同的限制，因此请检查结果简单功能类型
         *
         * Each data store has different limitations so check the resulting SimpleFeatureType.
         */
        System.out.println("SHAPE:" + SHAPE_TYPE);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            /*
             * SimpleFeatureStore有一个从SimpleFeatureCollection对象，
             * 所以我们使用ListFeatureCollection类来包装我们的功能列表。
             */
            SimpleFeatureCollection collection = new ListFeatureCollection(type, features);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
            System.exit(0); // success!
        } else {
            System.out.println(typeName + " does not support read/write access");
            System.exit(1);
        }
    }


}
