package com.meng.geo.shape.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ShapeProperty implements Serializable {

    private static final long serialVersionUID = -8986247433064482779L;
    /**
     * 字段名称
     */
    private String name;
    /**
     * 字段类型
     */
    private Class<?> type;

    public ShapeProperty() {
    }

    public ShapeProperty(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "字段名称 : " + name
                + "\n"
                + "字段类型 : " + type.getName()
                + "\n";
    }
}
