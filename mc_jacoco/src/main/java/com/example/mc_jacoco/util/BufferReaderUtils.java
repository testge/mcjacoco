package com.example.mc_jacoco.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author luping
 * @date 2023/12/7 23:35
 */
public class BufferReaderUtils {

    public static StringBuffer reader(String path) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            StringBuffer stringBuffer = new StringBuffer();
            String s = "";
            while ((s = bufferedReader.readLine()) != null) {
                stringBuffer.append(s.trim() + "\n");
            }
            return stringBuffer;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
