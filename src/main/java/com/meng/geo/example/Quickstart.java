package com.meng.geo.example;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.SpatialIndexFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 提示用户输入shapefile，并在屏幕上的地图框中显示内容。这是用于文档和教程的地理工具快速启动应用程序
 *
 * 关于代码示例的几点注意事项：
 *      shapefile不会加载到内存中-而是每次需要时都从磁盘读取它。这种方法使您可以处理大于可用内存的数据集。
 *      我们在这里使用非常基本的显示样式，仅显示功能轮廓。在下面的示例中，我们将看到如何指定更复杂的样式。
 */
public class Quickstart {

    public static void main(String[] args) throws Exception {

        String filePath = "";
        String title = "";

        Quickstart quickstart = new Quickstart();

        quickstart.method1(filePath, title);
        quickstart.method2(filePath, title);
    }

    /**
     * 高级：使用FileDataStoreFinder可以使我们轻松处理文件。
     * 另一种处理方法是使用连接参数映射。这种技术使我们对控制shapefile的方式有了更多的控制，
     * 还使我们可以连接到数据库和Web功能服务器。
     */
    private void method1(String filePath, String title) throws IOException {

        File shapeFile = getShapeFile(filePath);

        SimpleFeatureSource featureSource = createDataStore(shapeFile);

        MapContent map = createMapContent(featureSource, title);

        JMapFrame.showMap(map);
    }

    /**
     * 如果您想让GeoTools将shapefile缓存在内存中，请尝试以下代码：
     * 此方法演示了如何使用基于内存的缓存来加快显示速度(例如，放大和缩小时)。
     *
     * 与我们创建CachingFeatureStore实例的主方法相比，只多了一行。
     */
    private void method2(String filePath, String title) throws IOException {

        File shapeFile = getShapeFile(filePath);

        SimpleFeatureSource featureSource = createDataStore(shapeFile);

        SimpleFeatureSource cachedSource = getCachedSource(featureSource);

        MapContent map = createMapContent(featureSource, cachedSource, title);

        JMapFrame.showMap(map);
    }

    private File getShapeFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file;
        }
        throw new RuntimeException("文件不存在");
    }

    private SimpleFeatureSource createDataStore(File file) throws IOException {

        URL url = file.toURI().toURL();

        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("create spatial index", false);
        params.put("memory mapped buffer", false);
        params.put("charset", "GBK");

        DataStore store = DataStoreFinder.getDataStore(params);
        return store.getFeatureSource(store.getTypeNames()[0]);
    }

    private SimpleFeatureSource getCachedSource(SimpleFeatureSource featureSource) throws IOException {
        return DataUtilities.source(new SpatialIndexFeatureCollection(featureSource.getFeatures()));
    }

    /**
     * 创建一个地图内容，并将我们的形状文件添加到其中
     */
    private MapContent createMapContent(SimpleFeatureSource featureSource,
                                        SimpleFeatureSource cachedSource,
                                        String title) {

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(cachedSource, style);

        MapContent map = new MapContent();
        map.setTitle(title);
        map.addLayer(layer);

        return map;
    }

    private MapContent createMapContent(SimpleFeatureSource featureSource, String title) {

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);

        // 创建一个地图内容，并将我们的shapefile添加到其中
        MapContent map = new MapContent();
        map.setTitle(title);
        map.addLayer(layer);

        return map;
    }

}
