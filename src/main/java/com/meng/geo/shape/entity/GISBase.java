package com.meng.geo.shape.entity;

import com.meng.geo.shape.enums.GeomTypeEnum;
import lombok.Getter;
import org.opengis.feature.simple.SimpleFeature;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GIS对象基类
 */
@Getter
public class GISBase implements Serializable {

    private static final long serialVersionUID = 3421746183518108441L;
    /**
     * 对象类型
     */
    private final GeomTypeEnum type;
    /**
     * 特征
     */
    private SimpleFeature simpleFeature;
    /**
     * 属性
     */
    private List<ShapeProperty> propertieList = new ArrayList<>();
    /**
     * 属性信息<属性名称, 属性值>
     */
    private Map<String, Object> propertyMap = new HashMap<>();

    protected GISBase(GeomTypeEnum type, SimpleFeature simpleFeature) {
        this.type = type;
        this.simpleFeature = simpleFeature;
    }

}
