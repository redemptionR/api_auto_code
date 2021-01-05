package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.data.Constants;
import com.lemon.data.GlobalEnvironment;
import com.lemon.pojo.CaseInfo;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

/**
 * @author：yipiao
 * @Date：2020/12/31 - 12:29
 * 柠檬班
 */
class AddLoanTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        caseInfoList = getCaseDataFromExcel(4);         //获取第五个表（addloan）的数据
        caseInfoList = paramsReplace(caseInfoList);     //参数化，使用BaseCase中的regexReplace方法
    }

    @Test(dataProvider = "getAddLoanDatas")
    public void testAddLoan(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //请求头由json转化为Map
        Map headersMap =fromJsonToMap(caseInfo.getRequestHeader());
        //创建log目录
        String logFilePath =addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        Response res =
        given().log().all().
               headers(headersMap).
               body(caseInfo.getInputParams()).
        when().
               post(caseInfo.getUrl()).
        then().log().all().
               extract().response();
        Allure.addAttachment("接口请求的响应信息",new FileInputStream(logFilePath)); //allure报表
        //断言
        assertExpected(caseInfo,res);

        if(res.path("data.id")!=null) {
            //传给loan_id
            GlobalEnvironment.envData.put("loan_id", res.path("data.id"));
        }
    }

    @DataProvider
    public Object[] getAddLoanDatas() {
        return caseInfoList.toArray();
    }
}
