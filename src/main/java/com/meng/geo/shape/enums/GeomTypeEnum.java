package com.meng.geo.shape.enums;

/**
 * 几何类型枚举类
 */
public enum GeomTypeEnum {

    /**
     * 点
     */
    POINT(1),
    /**
     * 线
     */
    LINE(2),
    /**
     * 多边形
     */
    POLYGON(4);

    private int type;

    GeomTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
