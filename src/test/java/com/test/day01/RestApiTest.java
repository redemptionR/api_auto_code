package com.test.day01;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
/**
 * @author：yipiao
 * @Date：2020/12/17 - 12:45
 * 柠檬班
 */
public class RestApiTest {
        @Test
        public void TestGet01() {
            //1.====================================无参的get请求
            given().
                    //请求头，请求参数，请求体数据
                            when().
                    //所执行的操作，Url
                            get("http://httpbin.org/get").
                    then().
                    //解析结果，断言
                            log().all();
        }
        @Test
        public void TestGet02() {
        //2.====================================带参数的get请求---1
        given().
               //请求头，请求参数，请求体数据
        when().
               get("http://httpbin.org/get?name=张三&age=18").
        then().
               log().all();
        }

        @Test
        public void testGet03() {
        //2.====================================带参数的get请求---2
        given().
                queryParam("name", "张三").queryParam("age", 20).
        when().
                get("http://httpbin.org/get").
        then().
                log().all();
        }
        @Test
        public void testGet04() {
        //3.======================================带多个参数的get请求
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", "张三");
        map.put("age", "30");
        map.put("sex", "男");
        map.put("address", "北京");
        given().
                queryParams(map).
        when().
                get("http://httpbin.org/get").
        then().
                log().all();   //log（）.all打印所有的信息
                //log().body();
        }


         @Test
         public void testPost01() {
         //1.====================================Post请求，form
         given().
                formParam("name","李四").
                contentType("application/x-www-form-urlencoded;charset=utf-8").
         when().
                post("http://httpbin.org/post").
         then().
                log().all();
    }

        @Test
        public void testPost02() {
        //2.====================================Post请求，json格式进行传参
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("mobile_phone","15810508912");
            map.put("pwd","123456789");
        given().
                contentType("application/json;charset=utf-8").
                body(map).
        when().
                post("http://httpbin.org/post").
        then().
                log().all();
    }
    @Test
    public void testPost03() {
    //3.====================================Post请求，xml格式进行传参
        String xmlstr= "<?xml version=\"1.0\" encoding=\"utf-8\"?>"+
                "<suite>\n"+
                    "<class>测试XML</class>\n"+
                "<suite>";

    given().
            contentType("text/xml;charset=utf-8").
           body(xmlstr).
    when().
           post("http://httpbin.org/post").
    then().
           log().all();
    }

    @Test
    public void testPost04() {
        //4.====================================Post请求，多参数表单进行传参
        given().
                contentType("multipart/form-data;charset=utf-8").
                multiPart(new File("D:\\XML.txt")).     //multipart表示传文件
        when().
                post("http://httpbin.org/post").
        then().
                log().all();
    }
}

