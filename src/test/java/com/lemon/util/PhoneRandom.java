package com.lemon.util;

import java.util.Random;

public class PhoneRandom {
    /**
     * 随机生成一个手机号码
     */
    public static String getPhone(){
        //定义手机的号段
        String phonePrefix="133";
        //循环8次，把每一次生成的整数拼接上
        for(int i=0; i<8; i++) {
            //后面8位的话可以随机来生成
            Random random = new Random();
            //nextInt随机生成一个数，参数你可以去指定范围
            int num = random.nextInt(9);
            phonePrefix+=num;
        }
        return phonePrefix;
    }

    public static void main(String[] args) {
        //解决每次注册运行的时候手机号码需要手动更改的问题
        //1、先来随机生成手机号码（可能已经被注册过了）
        //2、查询数据库，如果有注册的话，再来随机生成
        //3、1-2循环运行，直到产生一个没有注册过的手机号码即可
        System.out.println(getRandomPhone());
    }

    /**
     * 获取一个数据库当中没有注册过的手机号码
     */
    public static String getRandomPhone(){
        while(true) {
            String phone = getPhone();
            Object result = JDBCUtils.querySingle("select count(*) from member where mobile_phone=" + phone);
            if ((Long) result == 1) {
                System.out.println("手机号码注册过");
            } else {
                return phone;
            }
        }
    }
}
