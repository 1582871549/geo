package com.meng.geo.map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.CursorTool;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import javax.swing.JButton;
import javax.swing.JToolBar;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SelectionLab {

    /*
     * 将用来创建样式和过滤对象的工厂
     */
    private StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2();

    /*
     * 几何类型
     */
    private enum GeomType {
        POINT,
        LINE,
        POLYGON
    };

    /*
     * 一些默认的样式变量
     */
    private static final Color LINE_COLOUR = Color.BLUE;
    private static final Color FILL_COLOUR = Color.CYAN;
    private static final Color SELECTED_COLOUR = Color.YELLOW;
    private static final float OPACITY = 1.0f;
    private static final float LINE_WIDTH = 1.0f;
    private static final float POINT_SIZE = 10.0f;

    private JMapFrame mapFrame;
    private SimpleFeatureSource featureSource;

    private String geometryAttributeName;
    private GeomType geometryType;

    /*
     * 使用方法
     */
    public static void main(String[] args) throws Exception {
        SelectionLab me = new SelectionLab();

        String filePath = "/Users/dujianwei/every-x/geo-test/test.shp";

        me.displayShapeFile(filePath);
    }

    /**
     * 展示shape文件
     *
     * 此方法连接到shapefile检索有关其功能的信息；
     * 创建地图框以显示shapefile，并将自定义要素选择工具添加到地图框的工具栏中。
     */
    private void displayShapeFile(String filePath) throws Exception {
        // 1、获取文件
        File shapeFile = readShapeFile(filePath);
        // 2、获取数据源
        createFeatureSource(shapeFile);
        // 3、设置几何对象名称及几何类型
        setGeometry();
        // 4、创建地图
        createMap();
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
    private void createFeatureSource(File shapeFile) throws IOException {
        // 读取shp文件到数据存储中
        Map<String, Object> params = new HashMap<>();
        params.put("url", shapeFile.toURI().toURL());
        params.put("create spatial index", false);
        params.put("memory mapped buffer", false);
        params.put("charset", "GBK");

        DataStore dataStore = DataStoreFinder.getDataStore(params);
        String[] typeNames = dataStore.getTypeNames();

        featureSource = dataStore.getFeatureSource(typeNames[0]);
    }

    // 检索关于特征几何的信息
    private void setGeometry() {
        GeometryDescriptor geomDesc = featureSource.getSchema().getGeometryDescriptor();
        geometryAttributeName = geomDesc.getLocalName();

        Class<?> clazz = geomDesc.getType().getBinding();

        if (Polygon.class.isAssignableFrom(clazz) || MultiPolygon.class.isAssignableFrom(clazz)) {
            geometryType = GeomType.POLYGON;
        } else if (LineString.class.isAssignableFrom(clazz) || MultiLineString.class.isAssignableFrom(clazz)) {
            geometryType = GeomType.LINE;
        } else {
            geometryType = GeomType.POINT;
        }
    }

    // 创建JMapFrame，并将其设置为使用默认线条和颜色样式显示形状文件的功能
    private void createMap() {

        MapContent map = new MapContent();
        map.setTitle("特征选择工具示例");

        Style style = createDefaultStyle();
        Layer layer = new FeatureLayer(featureSource, style);
        map.addLayer(layer);
        mapFrame = new JMapFrame(map);
        mapFrame.enableToolBar(true);
        mapFrame.enableStatusBar(true);

        // 在使地图框可见之前，我们为自定义要素选择工具的工具栏添加了一个新按钮
        JToolBar toolBar = mapFrame.getToolBar();
        JButton btn = new JButton("Select");
        toolBar.addSeparator();
        toolBar.add(btn);

        /*
         * 当用户点击按钮时，我们希望启用自定义功能选择工具。
         * 因为我们唯一感兴趣的鼠标动作是“点击”，
         * 我们在这里不创建控制图标或光标，
         * 我们可以将我们的工具创建为CursorTool的匿名子类。
         */
        btn.addActionListener(
                e ->
                        mapFrame.getMapPane()
                                .setCursorTool(
                                        new CursorTool() {

                                            @Override
                                            public void onMouseClicked(MapMouseEvent ev) {
                                                selectFeatures(ev);
                                            }
                                        }));

        /* 最后，我们显示地图框。关闭后，该应用程序将退出. */
        mapFrame.setSize(600, 600);
        mapFrame.setVisible(true);
    }

    /**
     * 当用户单击地图时，我们的要素选择工具会调用此方法。
     *
     * @param ev 正在处理的鼠标事件
     */
    private void selectFeatures(MapMouseEvent ev) {

        System.out.println("Mouse click at: " + ev.getWorldPos());

        /*
         * 以鼠标点击位置为中心构建一个5x5像素的矩形
         */
        Point screenPos = ev.getPoint();
        Rectangle screenRect = new Rectangle(screenPos.x - 2, screenPos.y - 2, 5, 5);

        /*
         * 将屏幕矩形转换为地图上下文的坐标参考系统中的边界框。注意:
         * 我们在这里使用了一个简单的方法，但是地理工具也提供了其他的，更精确的方法。
         */
        AffineTransform screenToWorld = mapFrame.getMapPane().getScreenToWorldTransform();
        Rectangle2D worldRect = screenToWorld.createTransformedShape(screenRect).getBounds2D();
        ReferencedEnvelope bbox =
                new ReferencedEnvelope(
                        worldRect, mapFrame.getMapContent().getCoordinateReferenceSystem());

        // 创建过滤器以选择与边界框相交的要素
        Filter filter = filterFactory.intersects(filterFactory.property(geometryAttributeName), filterFactory.literal(bbox));

        // 使用过滤器识别所选特征
        try {
            SimpleFeatureCollection selectedFeatures = featureSource.getFeatures(filter);

            Set<FeatureId> IDs = new HashSet<>();
            try (SimpleFeatureIterator iter = selectedFeatures.features()) {
                while (iter.hasNext()) {
                    SimpleFeature feature = iter.next();
                    IDs.add(feature.getIdentifier());

                    System.out.println("   " + feature.getIdentifier());
                }
            }

            if (IDs.isEmpty()) {
                System.out.println("   no feature selected");
            }

            displaySelectedFeatures(IDs);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 将显示设置为将选定要素绘制为黄色，并将未选定要素绘制为默认样式。
     *
     * @param IDs 当前选定要素的标识符
     */
    public void displaySelectedFeatures(Set<FeatureId> IDs) {
        Style style;

        if (IDs.isEmpty()) {
            style = createDefaultStyle();

        } else {
            style = createSelectedStyle(IDs);
        }

        Layer layer = mapFrame.getMapContent().layers().get(0);
        ((FeatureLayer) layer).setStyle(style);
        mapFrame.getMapPane().repaint();
    }

    // 为要素显示创建默认样式
    private Style createDefaultStyle() {
        Rule rule = createRule(LINE_COLOUR, FILL_COLOUR);

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        fts.rules().add(rule);

        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    /**
     * 创建一种样式，其中具有给定标识的要素被绘制为黄色，而其他要素被绘制为默认颜色。
     */
    private Style createSelectedStyle(Set<FeatureId> IDs) {
        Rule selectedRule = createRule(SELECTED_COLOUR, SELECTED_COLOUR);
        selectedRule.setFilter(filterFactory.id(IDs));

        Rule otherRule = createRule(LINE_COLOUR, FILL_COLOUR);
        otherRule.setElseFilter(true);

        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);

        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    /**
     * createXXXStyle方法的助手。创建一个新规则，该规则包含一个根据我们正在显示的要素的几何类型定制的符号生成器。
     */
    private Rule createRule(Color outlineColor, Color fillColor) {
        Symbolizer symbolizer = null;
        Fill fill = null;
        Stroke stroke = styleFactory.createStroke(filterFactory.literal(outlineColor), filterFactory.literal(LINE_WIDTH));

        switch (geometryType) {
            case POLYGON:
                fill = styleFactory.createFill(filterFactory.literal(fillColor), filterFactory.literal(OPACITY));
                symbolizer = styleFactory.createPolygonSymbolizer(stroke, fill, geometryAttributeName);
                break;

            case LINE:
                symbolizer = styleFactory.createLineSymbolizer(stroke, geometryAttributeName);
                break;

            case POINT:
                fill = styleFactory.createFill(filterFactory.literal(fillColor), filterFactory.literal(OPACITY));

                Mark mark = styleFactory.getCircleMark();
                mark.setFill(fill);
                mark.setStroke(stroke);

                Graphic graphic = styleFactory.createDefaultGraphic();
                graphic.graphicalSymbols().clear();
                graphic.graphicalSymbols().add(mark);
                graphic.setSize(filterFactory.literal(POINT_SIZE));

                symbolizer = styleFactory.createPointSymbolizer(graphic, geometryAttributeName);
        }

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }


}
