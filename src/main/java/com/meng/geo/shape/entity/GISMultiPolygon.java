package com.meng.geo.shape.entity;

import com.meng.geo.shape.enums.GeomTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.MultiPolygon;
import org.opengis.feature.simple.SimpleFeature;

@Getter
@Setter
public class GISMultiPolygon extends GISBase {

    private static final long serialVersionUID = -1494194513188558863L;
    /**
     * 多边形的geomtry模型。
     */
    private MultiPolygon polygon;

    public GISMultiPolygon(MultiPolygon polygon, SimpleFeature simpleFeature) {
        super(GeomTypeEnum.POLYGON, simpleFeature);
        this.polygon = polygon;
    }

}
