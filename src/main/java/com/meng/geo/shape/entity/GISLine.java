package com.meng.geo.shape.entity;

import com.meng.geo.shape.enums.GeomTypeEnum;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;

@Getter
@Setter
public class GISLine extends GISBase {

    private static final long serialVersionUID = -6579599110573430873L;
    /**
     * 多线geometry对象
     */
    private MultiLineString line;

    public GISLine(MultiLineString line, SimpleFeature simpleFeature) {
        super(GeomTypeEnum.LINE, simpleFeature);
        this.line = line;
    }


}
