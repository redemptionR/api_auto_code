package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.data.Constants;
import com.lemon.pojo.CaseInfo;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

/**
 * @author：yipiao
 * @Date：2020/12/28 - 15:37
 * 柠檬班
 */
public class RechargeTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        caseInfoList = getCaseDataFromExcel(3);
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getRechargeDatas")
    public void testRechargeInfo(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //请求头由json转化为Map
        Map headersMap =fromJsonToMap(caseInfo.getRequestHeader());
        //创建目录层级
        String logFilePath =addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        Response res =
        given().log().all().
                headers(headersMap).
                body(caseInfo.getInputParams()).
        when().
               post(caseInfo.getUrl()).
        then().log().all().
               extract().response();
        Allure.addAttachment("接口请求的响应信息",new FileInputStream(logFilePath));
        //断言
        assertExpected(caseInfo,res);
        //数据库的响应断言
        assertSQL(caseInfo);
    }

    @DataProvider
    public Object[] getRechargeDatas() {
        return caseInfoList.toArray();
    }
}
