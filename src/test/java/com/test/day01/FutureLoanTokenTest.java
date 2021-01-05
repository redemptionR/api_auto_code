package com.test.day01;

import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/18 - 13:04
 * 柠檬班
 */
public class FutureLoanTokenTest {
    @Test
    public void testLogin() {
        //2.====================================前程贷登陆
        String JsonStr = "{\"mobile_phone\":\"15810508913\",\"pwd\":\"123456789\"}";
        Response res =
        given().
               header("Content-Type", "application/json;charset=utf-8").
               header("X-Lemonban-Media-Type", "lemonban.v2").
               body(JsonStr).
        when().
               post("http://api.lemonban.com/futureloan/member/login").
        then().
               log().all().
               extract().response();
        //获取响应体里面的所有信息，包括响应头，响应体
        System.out.println(res.asString());  //对象res（类似ToString方法）
        //提取响应状态码（200）
        System.out.println(res.statusCode());  //statusCode()方法
        //提取响应头
        System.out.println(res.header("Content-Type"));  //响应头中的Content-Type
        //获取接口响应时间，单位为毫秒
        System.out.println(res.time());
        //提取响应体信息，Gpath：路径表达式语言（html,json.xml都可以用）,Gpath方法→使用Gpath路径表达式来提取
        String tokenValue = res.path("data.token_info.token");
        System.out.println(tokenValue);
        //提取memberId
        int memberId = res.path("data.id");
        System.out.println(memberId);

        //充值请求
        //2.====================================前程贷充值
        //把请求数据放入map中
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("member_id", memberId);
        map.put("amount", 10000);
        given().
                header("Content-Type", "application/json;charset=utf-8").
                header("X-Lemonban-Media-Type", "lemonban.v2").
                //接口文档规定，如果认证方式 鉴权方式是token，
                header("Authorization","Bearer "+tokenValue).
                body(map).
        when().
                post("http://api.lemonban.com/futureloan/member/recharge").
        then().
                log().all();
    }
}
