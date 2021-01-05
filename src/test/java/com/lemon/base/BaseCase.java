package com.lemon.base;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lemon.data.Constants;
import com.lemon.pojo.CaseInfo;
import com.lemon.data.GlobalEnvironment;
import com.lemon.util.JDBCUtils;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.config.JsonConfig.jsonConfig;
import static io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL;

/**
 * @author：yipiao
 * @Date：2020/12/31 - 16:51
 * 当前所有测试类中的所有数据，是放置所有公用的方所有测试用例类的父类
 */
public class BaseCase {

    @BeforeTest
    public void globalSetup() throws FileNotFoundException {
        //整体全局性前置配置/初始化
        //1.设置项目的baseurl
        RestAssured.baseURI="http://api.lemonban.com/futureloan";
        //2.配置结果为json返回的小数类型的，使用bigdecimal类型来存储
        //让ResAssured返回小数时，用BIG_DECIMAL存储小数的值（默认时Float存储）
        RestAssured.config=RestAssured.config().jsonConfig(jsonConfig().numberReturnType(BIG_DECIMAL));
        //PrintStream fileOutPutStream = new PrintStream(new File("log/test_all.log"));
        //RestAssured.filters(new RequestLoggingFilter(fileOutPutStream),new ResponseLoggingFilter(fileOutPutStream));
    }

    //提取数据
    public List<CaseInfo> getCaseDataFromExcel(int index) {
        //dataProvider返回值类型可是一维的Object数组，也可以是二维的
        //读取excel测似用例数据？？easyPOI（推荐）！
        //第一个参数：File对象  第二个参数为映射的实体类  第三个参数为读取的配置
        ImportParams importParams = new ImportParams();
        importParams.setStartSheetIndex(index);
        File excelFile = new File(Constants.EXECEL_PATH);
        List<CaseInfo> list = ExcelImportUtil.importExcel(excelFile, CaseInfo.class, importParams);
        return list;
    }

    public String addLogToFile(String interfaceName,int caseId) {
        String logFilePath="";
        if (!Constants.IS_DEBUG) {
            //创建目录层级
            String dirPath = "target/log/" + interfaceName;
            File dirFile = new File(dirPath);
            if (!dirFile.isDirectory()) {
                dirFile.mkdirs();
            }
            logFilePath = dirPath + "/" + interfaceName + "_" + caseId + ".log";
            PrintStream fileOutPutStream = null;
            try {
                fileOutPutStream = new PrintStream(new File(logFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            RestAssured.config = RestAssured.config().logConfig(LogConfig.logConfig().defaultStream(fileOutPutStream));
        }
        return logFilePath;
    }
    /*
     *1.把数据转换成Map
     *2.循环遍历取到Map里的每一对
     */
    public void assertExpected(CaseInfo caseInfo, Response res){
        ObjectMapper objectMapper2 = new ObjectMapper();
        Map expectedMap = null;
        try {
            expectedMap = objectMapper2.readValue(caseInfo.getExpected(), Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Set<Map.Entry<String, Object>> set = expectedMap.entrySet();
        for (Map.Entry<String, Object> map : set) {
            //判断类型(expect是不是Float类型的，instanceof)
            Object expected = map.getValue();
            if(expected instanceof Float ||expected instanceof Double){
                //Float类型转化成BigDecimal类型
                BigDecimal bigDecimalData =new BigDecimal(map.getValue().toString());
                Assert.assertEquals(res.path(map.getKey()),bigDecimalData,"接口响应断言失败");
            }else {
                Assert.assertEquals(res.path(map.getKey()), expected,"接口响应断言失败");
            }
            //System.out.println(map.getKey());
            //System.out.println(map.getValue());
            //做断言！通过Gpath里面的每一对键值对
            //直接设计成Gpath表达式，map.getKey()可以直接获取到键名
            //键值直接就是期望的值(期望的json结果为小数  double/float)
            //把期望值也转化为BIG_DECIMAL。
            //map.getValue()判断是不是小数类型的
        }
    }

    /*
    * 数据库的断言
    * */
    public void assertSQL(CaseInfo caseInfo) {
        String checkSQL = caseInfo.getCheckSQL();
        if (checkSQL != null) {
            Map checkSQLMap = fromJsonToMap(checkSQL);
            Set<Map.Entry<String, Object>> Set = checkSQLMap.entrySet();
            for (Map.Entry<String, Object> mapEntry : Set) {
                String Sql = mapEntry.getKey();
                Object actual = JDBCUtils.querySingle(Sql);
                //数据库查询结果返回类型为Long类型，在excel中获取的期望值是Interger类型
                if (actual instanceof Long) {
                    //把期望的转化为Long类型
                    Long expected = new Long(mapEntry.getValue().toString());
                    Assert.assertEquals(actual, expected,"数据库断言失败");
                    //System.out.println("long类型与Interger类型做断言");
                }else if (actual instanceof BigDecimal){
                    BigDecimal expected = new BigDecimal(mapEntry.getValue().toString());
                    Assert.assertEquals(actual, expected,"数据库断言失败");
                    //System.out.println("Double类型与BigDecimal类型做断言");
                }else{
                    Assert.assertEquals(actual,mapEntry.getValue(),"数据库断言失败");
                    //System.out.println("字符串类型做断言");
                }
                //数据库查询结果为bigdecimal类型，在excel中获取的期望值是Double类型
            }
        }
    }


    //对所有的case进行参数化处理
    public List<CaseInfo> paramsReplace(List<CaseInfo> caseInfoList) {
        //对四块做参数化处理（请求头、接口地址、参数输入、期望返回结果）
        for (CaseInfo caseInfo : caseInfoList) {
            //参数化的处理
            String requestHeader = regexReplace(caseInfo.getRequestHeader());
            caseInfo.setRequestHeader(requestHeader);
            String url = regexReplace(caseInfo.getUrl());
            caseInfo.setUrl(url);
            String inputParams = regexReplace(caseInfo.getInputParams());
            caseInfo.setInputParams(inputParams);
            String expected = regexReplace(caseInfo.getExpected());
            caseInfo.setExpected(expected);
            String checkSQL=regexReplace(caseInfo.getCheckSQL());
            caseInfo.setCheckSQL(checkSQL);
        }
        return caseInfoList;
    }


    //对一条case进行参数化替换
    public CaseInfo paramsReplaceCaseInfo(CaseInfo caseInfo) {
        //对四块做参数化处理（请求头、接口地址、参数输入、期望返回结果）
            String requestHeader = regexReplace(caseInfo.getRequestHeader());
            caseInfo.setRequestHeader(requestHeader);

            String url = regexReplace(caseInfo.getUrl());
            caseInfo.setUrl(url);

            String inputParams = regexReplace(caseInfo.getInputParams());
            caseInfo.setInputParams(inputParams);

            String expected = regexReplace(caseInfo.getExpected());
            caseInfo.setExpected(expected);

            String checkSQL=regexReplace(caseInfo.getCheckSQL());
            caseInfo.setCheckSQL(checkSQL);
            return caseInfo;
    }


    //正则表达式转化
    public String regexReplace(String sourceStr) {
        //如果参数化的原字符串为空，就不用去参数化的过程
        if(sourceStr ==null){
            return  sourceStr;
        }
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
            Object replaceStr = GlobalEnvironment.envData.get(singleStr);
            sourceStr =sourceStr.replace(findStr, replaceStr + "");
        }
        return sourceStr;
    }


    public Map fromJsonToMap(String jsonStr){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonStr,Map.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

}
