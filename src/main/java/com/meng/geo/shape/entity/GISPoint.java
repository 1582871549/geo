package com.meng.geo.shape.entity;

import com.meng.geo.shape.enums.GeomTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;

/**
 * 点对象模型
 */
@Getter
@Setter
public class GISPoint extends GISBase {

    private static final long serialVersionUID = 7850323808857138095L;
    /**
     * 点geometry对象
     */
    private Point point;

    public GISPoint(Point point, SimpleFeature simpleFeature) {
        super(GeomTypeEnum.POINT, simpleFeature);
        this.point = point;
    }
}
