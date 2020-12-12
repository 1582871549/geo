package com.meng.geo;

import com.meng.geo.shape.entity.GISBase;
import com.meng.geo.shape.utils.ShapeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

/**
 * 测试驱动开发
 *      没有测试之前不要写任何功能代码
 *      只编写恰好能够体现一个失败情况的测试代码
 *      只编写恰好能通过测试的功能代码
 *
 * 测试的FIRST准则
 *      快速（Fast）测试应该够快，尽量自动化。
 *      独立（Independent） 测试应该应该独立。不要相互依赖
 *      可重复（Repeatable） 测试应该在任何环境上都能重复通过。
 *      自我验证（Self-Validating） 测试应该有bool输出。不要通过查看日志这种低效率方式来判断测试是否通过
 *      及时（Timely） 测试应该及时编写，在其对应的生产代码之前编写
 *
 * 整洁代码准则
 *      优雅且高效、直截了当、减少依赖、只做好一件事
 *      简单直接
 *      可读、可维护、单元测试
 *      不要重复、单一职责、表达力
 */
@SpringBootTest
class GeoTests {

    /**
     * 通过这种方式读取的属性MAP表中存储了GIS对象的坐标信息，
     * 对应的key是the_geom。多变形和线的the_geom值很长，如果需要在在界面展示属性，可以把它去掉。
     */
    @Test
    void sout1Test() throws IOException {

        String filePath = "/Users/dujianwei/Downloads/新建下载4百度POI_45131_28756_16/新建下载4百度POI_45131_28756_16.shp";

        ShapeUtils utils = new ShapeUtils();

        List<GISBase> gisBases = utils.readGisObject(filePath);

        for (GISBase gisBase : gisBases) {
            System.out.println(gisBase);
        }

        System.out.println("Start GeoTools success!");
    }

}
