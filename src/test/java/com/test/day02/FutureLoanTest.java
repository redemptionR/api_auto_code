package com.test.day02;

import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/18 - 12:50
 * 柠檬班
 */
public class FutureLoanTest {
    @Test
    public void testRegister(){
        //1.====================================前程贷注册
        String JsonStr="{\"mobile_phone\":\"15810508913\",\"pwd\":\"123456789\",\"type\":\"1\"}";
        given().
                //contentType("application/json;charset=utf-8").
                header("Content-Type","application/json;charset=utf-8").
                header("X-Lemonban-Media-Type","lemonban.v1").
                body(JsonStr).
        when().
                post("http://api.lemonban.com/futureloan/member/register").
        then().
                log().all();
    }

    @Test
    public void testLogin(){
        //2.====================================前程贷登陆
        String JsonStr="{\"mobile_phone\":\"15810508913\",\"pwd\":\"123456789\"}";
        given().
                header("Content-Type","application/json;charset=utf-8").
                header("X-Lemonban-Media-Type","lemonban.v1").
                body(JsonStr).
        when().
                post("http://api.lemonban.com/futureloan/member/login").
        then().
                log().all();
    }
}
