package com.test.day02;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/20 - 16:14
 * 柠檬班
 */
public class AssertTest {
    /*
    * 断言操作
    */

    @Test
    public void testLogin(){
        //2.====================================前程贷登陆
        String JsonStr="{\"mobile_phone\":\"15810508913\",\"pwd\":\"123456789\"}";
        Response res=
        given().
                header("Content-Type","application/json;charset=utf-8").
                header("X-Lemonban-Media-Type","lemonban.v1").
                body(JsonStr).
        when().
                post("http://api.lemonban.com/futureloan/member/login").
        then().
                log().all().
                extract().response();

        //选取重要字段断言（code,msg等）
        //获取业务码
        int code =res.path("code");
        //获取msg
        String Msg = res.path("msg");
        //获取mobile_phone
        String mobilePhone =res.path("data.mobile_phone");
        //断言使用testng的框架所提供的API
        //第一个参数为实际值，第二个为期望值
        Assert.assertEquals(code,0);
        Assert.assertEquals(Msg,"OK");
        Assert.assertEquals(mobilePhone,"15810508913","断言失败");  //可以支持第三参数，提示信息
        Assert.assertTrue(Msg.equals("OK"));
        //Assert.assertFalse(Msg.equals("OK"));
    }
}
