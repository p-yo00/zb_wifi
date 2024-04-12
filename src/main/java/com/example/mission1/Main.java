package com.example.mission1;

import com.example.mission1.vo.WifiInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(WifiInfo.class.getDeclaredFields()));
        for (Field field : WifiInfo.class.getDeclaredFields()) {
            System.out.print(field.getName() + " ");
            if (field.getType() == Double.class) {
                System.out.println("double입니다");
            } else if (field.getType() == String.class) {
                System.out.println("string입니다.");
            } else {
                System.out.println();
            }

        }

        WifiInfo wifiInfo = new WifiInfo();
        wifiInfo.setWifiNm("wifi");
        wifiInfo.setYear("2024");
        Object obj = wifiInfo;

        Class clazz = WifiInfo.class;
        Field[] fields = clazz.getDeclaredFields();

        // 각 필드의 이름과 값 출력
        for (Field field : fields) {
            // 필드 이름 출력
            System.out.print("Field Name: " + field.getName() + ", ");

            // 필드의 접근성 설정
            field.setAccessible(true);

            try {
                // 필드 값 출력
                Object value = field.get(obj);
                System.out.println("Field Value: " + value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream("key.properties"));
            System.out.println(prop);
            System.out.println(prop.get("key"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
