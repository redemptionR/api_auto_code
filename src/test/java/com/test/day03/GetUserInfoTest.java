package com.test.day03;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
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
public class GetUserInfoTest {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        caseInfoList = getCaseDataFromExcel(2);
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getUserInfoDatas")
    public void testGetUserInfo(CaseInfo caseInfo) throws JsonProcessingException {
        //请求头由json转化为Map
        ObjectMapper objectMapper = new ObjectMapper();
        Map headersMap = objectMapper.readValue(caseInfo.getRequestHeader(), Map.class);
        Response res =
        given().
               headers(headersMap).
        when().
               get("http://api.lemonban.com/futureloan" + caseInfo.getUrl()).
        then().
               extract().response();
        //断言
        /*
         *1.把数据转换成Map
         *2.循环遍历取到Map里的每一对
         */
        ObjectMapper objectMapper2 = new ObjectMapper();
        Map expectedMap = objectMapper2.readValue(caseInfo.getExpected(), Map.class);
        Set<Map.Entry<String, Object>> set = expectedMap.entrySet();
        for (Map.Entry<String, Object> map : set) {
            System.out.println(map.getKey());
            System.out.println(map.getValue());
            //做断言！通过Gpath里面的每一对键值对
            //直接设计成Gpath表达式，map.getKey()可以直接获取到键名
            //键值直接就是期望的值
            Assert.assertEquals(res.path(map.getKey()), map.getValue());
        }
    }

    @DataProvider
    public Object[] getUserInfoDatas() {
        return caseInfoList.toArray();
    }

    public List<CaseInfo> getCaseDataFromExcel(int index) {
        //dataProvider返回值类型可是一维的Object数组，也可以是二维的
        //读取excel测似用例数据？？easyPOI（推荐）！
        //第一个参数：File对象  第二个参数为映射的实体类  第三个参数为读取的配置
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(index);
        File excelFile = new File("src/test/resources/api_testcases_futureloan_v2.xls");
        List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class, importParams);
        return list;
    }

    /*
     * 正则的替换
     * sourceStr 是原始的字符串
     * return 是查找到匹配的内容返回
     *
     * */
    public String regexReplace(String sourceStr) {
        //1.定义正则表达式
        String regex = "\\{\\{(.*?)\\}\\}";
        //2.通过正则表达
        Pattern pattern = Pattern.compile(regex);
        //3.开始进行匹配（匹配器），参数就是要去哪一个字符串里进行匹配
        Matcher matcher = pattern.matcher(sourceStr);
        String findStr = "";
        String singleStr = "";
        //4.连续的查找匹配
        while (matcher.find()) {
            //输出找到的匹配结果，0为整个正则匹配到对应的内容输出，1为除掉大括号里面的内容如那个
            findStr = matcher.group(0);
            singleStr = matcher.group(1);
            //System.out.println(findStr);
            //System.out.println(singleStr);
        }
        //5.先去找到环境变量里面的值
        Object replaceStr = GlobalEnvironment.envData.get(singleStr);
        //6.替换原始字符串中的内容
        //System.out.println(findStr);
        //System.out.println(replaceStr + "");
        return sourceStr.replace(findStr, replaceStr + "");
    }

    public List<CaseInfo> paramsReplace(List<CaseInfo> caseInfoList) {
        //对四块做参数化处理（请求头、接口地址、参数输入、期望返回结果）
        for (CaseInfo caseInfo : caseInfoList) {
            //如果数据是为空的，没有必要去进行参数化的处理
            if (caseInfo.getRequestHeader() != null) {
                String requestHeader = regexReplace(caseInfo.getRequestHeader());
                caseInfo.setRequestHeader(requestHeader);
            }
            if (caseInfo.getUrl() != null) {
                String url = regexReplace(caseInfo.getUrl());
                caseInfo.setUrl(url);
            }
            if (caseInfo.getInputParams() != null) {
                String inputParams = regexReplace(caseInfo.getInputParams());
                caseInfo.setInputParams(inputParams);
            }
            if (caseInfo.getExpected() != null) {
                String expected = regexReplace(caseInfo.getExpected());
                caseInfo.setExpected(expected);
            }
        }
        return caseInfoList;
    }



    public static void main(String[] args) {
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
}
