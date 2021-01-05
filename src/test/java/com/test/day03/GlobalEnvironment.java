package com.test.day03;

import java.util.HashMap;
import java.util.Map;

/**
 * @author：yipiao
 * @Date：2020/12/21 - 19:10
 * 柠檬班
 */
public class GlobalEnvironment {
    //全局变量static
    //静态变量memberId全局共享-----→静态共享类
    //public static Integer memberId = 0;
    //优化设计---环境变量
    //换成Map形式保存环境变量
    public static Map<String,Object> envData = new HashMap<String, Object>();
}
