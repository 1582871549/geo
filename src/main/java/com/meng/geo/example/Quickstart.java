package com.meng.geo.example;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;

import javax.swing.UIManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 提示用户输入shapefile，并在屏幕上的地图框中显示内容。这是用于文档和教程的地理工具快速启动应用程序
 */
public class Quickstart {

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

        Quickstart quickstart = new Quickstart();

        quickstart.method1();
        // quickstart.method2();

    }

    private void method1() throws IOException {

        // 显示shapefiles的数据存储文件选择器对话框
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        SimpleFeatureSource featureSource = store.getFeatureSource();

        // 创建一个地图内容，并将我们的shapefile添加到其中
        MapContent map = new MapContent();
        map.setTitle("Quickstart");

        Style style = SLD.createSimpleStyle(featureSource.getSchema());
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);

        // Now display the map
        JMapFrame.showMap(map);
    }

    private void method2() throws IOException {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        URL url = file.toURI().toURL();

        Map<String, Object> params = new HashMap<>();
        params.put("url", url);
        params.put("create spatial index", false);
        params.put("memory mapped buffer", false);
        params.put("charset", "ISO-8859-1");

        DataStore store = DataStoreFinder.getDataStore(params);
        SimpleFeatureSource featureSource = store.getFeatureSource(store.getTypeNames()[0]);

        System.out.println(featureSource);
    }

}
