package me.chloe.moonlight.util;

public class StringUtil {
    public static boolean isStringUpperCase(String str){
        char[] charArray = str.toCharArray();
        for(int i=0; i < charArray.length; i++){
            if( !Character.isUpperCase( charArray[i] ))
                return false;
        }
        return true;
    }
}
