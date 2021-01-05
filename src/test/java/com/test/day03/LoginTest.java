package com.test.day03;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/20 - 17:36
 * List<CaseInfo> caseInfoList;变量
 * @BeforeTest  ---------------caseInfoList = getCaseDataFromExcel(1);
 * 1.原始字符串json转化成map-------new ObjectMapper();实例化
 *                ---objectMapper.readValue方法（第一个参数：json字符串（实体类） 第二个参数：转换成类型（Map.class））
 * 2.Response res = ....extract().response();(三段式)
 * 3.断言操作------new ObjectMapper();实例化
 *         ------objectMapper.readValue方法（第一个参数：json字符串（实体类） 第二个参数：转换成类型（Map.class））
 *         ------expectedMap.entrySet()方法（set遍历）
 *         ------Assert.assertEquals(res.path(map.getKey()), map.getValue());（断言传值与期望值）
 *         ------if(memberId !=null) （if语句防止null）
 *
 * 1.dataProvider：------public Object[] getLoginDatas()；返回list集合的值
 * 2.-------------------public List<CaseInfo> getCaseDataFromExcel(int index)创建list集合
 *                ------ImportParams importParams = new ImportParams(); 参数
 *                 ----importParams.setStartSheetIndex(index); 传递index索引
 *                ----new File用例文件
 *                ----ExcelImportUtil.importExcel获取数据（第一个参数：File对象  第二个参数为映射的实体类  第三个参数为读取的配置）
 *                ----return list
 *
 * 1.主函数--------new ImportParams();参数
 *          -----importParams.setStartSheetIndex(0);起始sheet
 *          -----importParams.setSheetNum(2); 读几个sheet
 *          -----new file（创建文件对象）
 *          -----ExcelImportUtil.importExcel（第一个参数：File对象  第二个参数为映射的实体类  第三个参数为读取的配置）
 *          --List<CaseInfo> list返回的是实体类对象
 */
public class LoginTest {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        caseInfoList = getCaseDataFromExcel(1);
    }

    @Test(dataProvider = "getLoginDatas")
    public void testLogin01(CaseInfo caseInfo) throws JsonProcessingException {
        /*实现思路：jackson依赖 → json转化成map
         *1.把原始字符串用json数据类型进行保存
         *2.通过ObjectMapper转换为Map
         */

        //实例化objectMapper这个对象
        ObjectMapper objectMapper = new ObjectMapper();
        //readValue()  第一个参数：json字符串 第二个参数：转换成类型（Map）
        Map headersMap = objectMapper.readValue(caseInfo.getRequestHeader(), Map.class);
        Response res =
        given().
               headers(headersMap).
               body(caseInfo.getInputParams()).
        when().
               post("http://api.lemonban.com/futureloan" + caseInfo.getUrl()).
        then().
                log().all().
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
            //System.out.println(map.getKey());
            //System.out.println(map.getValue());
            //做断言！通过Gpath里面的每一对键值对
            //直接设计成Gpath表达式，map.getKey()可以直接获取到键名
            //键值直接就是期望的值
            Assert.assertEquals(res.path(map.getKey()), map.getValue());
        }
        Integer memberId = res.path("data.id");
        if (memberId != null) {
            //GlobalEnvironment.memberId = memberId;
            GlobalEnvironment.envData.put("member_id", memberId);  //hashmap形式取存储
            //System.out.println(memberId);
            //找到正向用例的token
            String token = res.path("data.token_info.token");
            GlobalEnvironment.envData.put("token",token);
        }

    }


    @DataProvider
    public Object[] getLoginDatas() {
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


    public static void main(String[] args) {
//读取excel测似用例数据？？easyPOI（推荐）！
        //第一个参数：File对象  第二个参数为映射的实体类  第三个参数为读取的配置
        ImportParams importParams = new ImportParams();
        //开始读
        importParams.setStartSheetIndex(0);
        //读几个
        importParams.setSheetNum(2);
        File excelFile = new File("src/test/resources/api_testcases_futureloan_v1.xls");
        List<CaseInfo> list =ExcelImportUtil.importExcel(excelFile, CaseInfo.class,importParams);
        for (CaseInfo caseInfo1 :list) {
            System.out.println(caseInfo1);
        }
    }
}