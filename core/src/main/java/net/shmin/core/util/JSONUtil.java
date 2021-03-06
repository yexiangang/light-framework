package net.shmin.core.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class JSONUtil {

    private static String readStringFromFile(String path) throws IOException {
        InputStream inputStream = JSONUtil.class.getResourceAsStream(path);
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        String str;
        while ((str = bufferedReader.readLine()) != null) {
            stringBuilder.append(str);
        }
        String jsonString = stringBuilder.toString();
        return jsonString;
    }

    public static <T> T readFromFile(String path, Class<T> tClass) throws IOException {
        String jsonString = readStringFromFile(path);
        return JSON.parseObject(jsonString, tClass);
    }

    public static JSONObject readJSONObjectFromFile(String path) throws IOException {
        String jsonString = readStringFromFile(path);
        return JSON.parseObject(jsonString);
    }

}

