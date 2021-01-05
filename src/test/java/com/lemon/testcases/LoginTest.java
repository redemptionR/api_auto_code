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
import io.restassured.RestAssured;
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

import static io.restassured.RestAssured.given;

/**
 * @author：yipiao
 * @Date：2020/12/20 - 17:36
 * List<CaseInfo> caseInfoList;变量
 *  ---------------caseInfoList = getCaseDataFromExcel(1);
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
public class LoginTest extends BaseCase {
    List<CaseInfo> caseInfoList;

    @BeforeClass
    public void setup() {
        caseInfoList = getCaseDataFromExcel(1);
        caseInfoList = paramsReplace(caseInfoList);
    }

    @Test(dataProvider = "getLoginDatas")
    public void testLogin(CaseInfo caseInfo) throws JsonProcessingException, FileNotFoundException {
        /*实现思路：jackson依赖 → json转化成map
         *1.把原始字符串用json数据类型进行保存
         *2.通过ObjectMapper转换为Map
         */

        //实例化objectMapper这个对象
        //readValue()  第一个参数：json字符串 第二个参数：转换成类型（Map）
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
                log().all().
               extract().response();
        Allure.addAttachment("接口请求的响应信息",new FileInputStream(logFilePath));
        //断言
        /*
         *1.把数据转换成Map
         *2.循环遍历取到Map里的每一对
         */
        assertExpected(caseInfo,res);

        if(caseInfo.getCaseId()==1) {
            GlobalEnvironment.envData.put("token1",res.path("data.token_info.token"));
        }else if (caseInfo.getCaseId()==2) {
            GlobalEnvironment.envData.put("token2",res.path("data.token_info.token"));
        }else if (caseInfo.getCaseId() == 3) {
            GlobalEnvironment.envData.put("token3",res.path("data.token_info.token"));
        }
    }

    @DataProvider
    public Object[] getLoginDatas() {
        return caseInfoList.toArray();
    }
}

  /*  public static void main(String[] args) {
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
}*/