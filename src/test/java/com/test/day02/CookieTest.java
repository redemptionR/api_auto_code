package com.test.day02;

import org.testng.annotations.Test;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;


import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/19 - 13:03
 * 柠檬班
 */
public class CookieTest {
    /*
     * cookie+session的鉴权方式！！！
     */
    Map<String, String> cookiemap = new HashMap<String, String>();
    @Test(priority = 1)
    public void TestAuthenticationWithSession(){
        //登陆请求
        Response res=
        //三段式（header，fiddler获取登陆的form表单获取sessionId）（post请求）（响应操作）
        given().
               header("Content-Type","application/x-www-form-urlencoded;charset=UTF-8").
               header("X-Lemonban-Media-Type", "lemonban.v2").
               formParam("loginame","admin").formParam("password","e10adc3949ba59abbe56e057f20f883e").
        when().
               post("http://erp.lemfix.com/user/login ").
        then().
                log().all().
               extract().response();
        //System.out.println(res.header("Set-Cookie"));
        //获取sessionid（推荐）
        //System.out.println("cookie::"+res.getCookies());    //也可以返回token字段的，代码一样
        cookiemap=res.getCookies();       //拿到了seesionId
    }

    @Test(priority = 2)
    public void testxxx(){
        //getuserSession请求,必须要携带cookie里保存的SessionId
        given().
                //cookies方法直接获取里面的信息。
                cookies(cookiemap).
        when().
                get("http://erp.lemfix.com/user/getUserSession").
        then().
                log().all();
    }
}
