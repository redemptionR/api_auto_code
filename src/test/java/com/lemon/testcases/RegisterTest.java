package com.lemon.testcases;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.base.BaseCase;
import com.lemon.data.GlobalEnvironment;
import com.lemon.pojo.CaseInfo;
import com.lemon.util.PhoneRandom;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.*;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/26 - 17:04
 * 柠檬班
 */
public class RegisterTest extends BaseCase {
    //读取用例中的数据必备操作
    List<CaseInfo> caseInfoList;
    @BeforeClass
    public void setup() {
        //读取用例数据
        caseInfoList = getCaseDataFromExcel(0);
    }

    @Test(dataProvider = "getRegisterDatas")
        public void testRegister(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        //随机生成3个没有注册过的手机号
        //存到环境变量中
        if (caseInfo.getCaseId() == 1){
            String mobilePhone1 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone1",mobilePhone1);

        }else if (caseInfo.getCaseId() == 2){
            String mobilePhone2 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone2",mobilePhone2);
        }else if (caseInfo.getCaseId() == 3){
            String mobilePhone3 = PhoneRandom.getRandomPhone();
            GlobalEnvironment.envData.put("mobile_phone3", mobilePhone3);
        }
        //对当前的case进行参数化替换
        caseInfo = paramsReplaceCaseInfo(caseInfo);

        Map headersMap =fromJsonToMap(caseInfo.getRequestHeader());
        //创建目录层级
        String logFilePath =addLogToFile(caseInfo.getInterfaceName(),caseInfo.getCaseId());
        Response res =
        given().
               log().all().
               headers(headersMap).
               body(caseInfo.getInputParams()).
        when().
               post(caseInfo.getUrl()).
        then().log().all().
               extract().response();
        //接口请求/响应结束以后，把信息添加到ALLURE中
        //1.第一个参数为附件的名字
        //2.第二个为InputStream
        Allure.addAttachment("接口请求的响应信息",new FileInputStream(logFilePath));
        //断言响应结果
        assertExpected(caseInfo,res);
        //断言数据库
        assertSQL(caseInfo);


        //注册成功的密码从里面拿
        //JSON串，所有转Map,get方法拿到pwd值
        String inputParams = caseInfo.getInputParams();
        ObjectMapper objectMapper1 = new ObjectMapper();
        Map inputParamsMap = objectMapper1.readValue(inputParams,Map.class);
        Object pwd = inputParamsMap.get("pwd");
            if(caseInfo.getCaseId()==1){
                GlobalEnvironment.envData.put("mobile_phone1",res.path("data.mobile_phone"));
                GlobalEnvironment.envData.put("member_id1",res.path("data.id"));
                GlobalEnvironment.envData.put("pwd1",pwd+"");
            }else if (caseInfo.getCaseId() == 2){
                GlobalEnvironment.envData.put("mobile_phone2",res.path("data.mobile_phone"));
                GlobalEnvironment.envData.put("member_id2",res.path("data.id"));
                GlobalEnvironment.envData.put("pwd2",pwd+"");
            }else if(caseInfo.getCaseId() == 3){
                GlobalEnvironment.envData.put("mobile_phone3",res.path("data.mobile_phone"));
                GlobalEnvironment.envData.put("member_id3",res.path("data.id"));
                GlobalEnvironment.envData.put("pwd3",pwd+"");
            }
        }
        @DataProvider
        public Object[] getRegisterDatas() {
            return caseInfoList.toArray();
        }
    }
