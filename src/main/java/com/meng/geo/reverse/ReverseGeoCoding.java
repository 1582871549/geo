package com.meng.geo.reverse;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class ReverseGeoCoding {

    /**
     * 属性名称 : the_geom
     * 属性的值 : POINT (113.71968484245943 34.755453380266665)
     *
     * 属性名称 : name
     * 属性的值 : 中国共产党河南省浙江商人联合支部委员会
     *
     * 属性名称 : address
     * 属性的值 : 河南省郑州市金水区中州大道与中州大道出口交叉口西100米
     *
     * 属性名称 : location
     * 属性的值 : 113.719685,34.755453
     *
     * 属性名称 : tel
     * 属性的值 :
     *
     * 属性名称 : type
     * 属性的值 : 政府机构;各级政府
     *
     * 属性名称 : uid
     * 属性的值 : 8dd722ed2d7af031b6f42f71
     */

    public static void main(String[] args) {

        ReverseGeoCoding andLong = new ReverseGeoCoding();

        String output = "json";
        String coordtype = "wgs84ll";
        String lng = "34.755453380266665";
        String lat = "113.71968484245943";

        String url = andLong.getUrl(output, coordtype, lng, lat);

        System.out.println(url);

        String s = reverseGeocode(url);
    }

    /**
     * ak = cZzVyjGz5ZE9kaA7jFK33T16Ktl4YrGe
     *
     * http://api.map.baidu.com/reverse_geocoding/v3/?ak=cZzVyjGz5ZE9kaA7jFK33T16Ktl4YrGe&output=json&coordtype=wgs84ll&location=31.225696563611,121.49884033194  //GET请求
     */


    private static final String PREFIX = "http://api.map.baidu.com/reverse_geocoding/v3/?ak=";
    private static final String AK = "cZzVyjGz5ZE9kaA7jFK33T16Ktl4YrGe";

    /**
     * lat<纬度>,lng<经度>
     */
    private String getUrl(String output, String coordtype, String lng, String lat) {
        return PREFIX + AK +
                "&output=" + output +
                "&coordtype=" + coordtype +
                "&location=" + lng + "," + lat;
    }

    public static String reverseGeocode(String url) {


        String res = doGet(url);

        System.out.println("res : " + res);

        String Addresslocation = JSON.parseObject(res).getJSONObject("result").getString("formatted_address");

        System.out.println(Addresslocation);
        return Addresslocation;
    }

    public static String doGet(String url) {

        // 创建一个Http客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        // 创建一个get请求
        HttpGet httpGet = new HttpGet(url);
        //由客户端发送get请求
        try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

            //从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                return EntityUtils.toString(responseEntity);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
