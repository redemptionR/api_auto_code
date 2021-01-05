package com.lemon.testcases;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.data.Constants;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/22 - 16:52
 * 柠檬班
 */
public class GetUserInfoTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        caseInfoList = getCaseDataFromExcel(2);
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getUserInfoDatas")
    public void testGetUserInfo(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //请求头由json转化为Map
        Map headersMap =fromJsonToMap(caseInfo.getRequestHeader());
        //创建目录层级
        String logFilePath =addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        Response res =
        given().log().all().
                headers(headersMap).
        when().
               get(caseInfo.getUrl()).
        then().log().all().
               extract().response();
        Allure.addAttachment("接口请求的响应信息",new FileInputStream(logFilePath));
        //断言
        assertExpected(caseInfo, res);
    }

    @DataProvider
    public Object[] getUserInfoDatas() {
        return caseInfoList.toArray();
    }
}
    /*public static void main(String[] args) {
        Integer memberId = 2111;
        String str1="/member/{{member_id}}/info";
        String str2="{\n" +
                "    \"code\": 0,\n" +
                "    \"msg\": \"OK\",\n" +
                "    \"data.id\": {{member_id}},\n" +
                "\"data.mobile_phone\": \"13323234110\"\n" +
                "}";
        //参数化的替换功能
        //正则表达式---
        // "."(匹配任意的字符)
        // "*"(前面的字符0次或者任意次数)
        // "?"(匹配前面的子表达式零次或一次，或指明一个非贪婪限定符)贪婪匹配
        //.*?
        //1.定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2.通过正则表达
        Pattern pattern = Pattern.compile(regex);
        //3.开始进行匹配（匹配器），参数就是要去哪一个字符串里进行匹配
        Matcher matcher = pattern.matcher(str1);
        //4.连续的查找匹配
        while(matcher.find()){
            //输出找到的匹配结果
            System.out.println(matcher.group(1));
        //5.替换原始字符串
        }
    }
}*/
