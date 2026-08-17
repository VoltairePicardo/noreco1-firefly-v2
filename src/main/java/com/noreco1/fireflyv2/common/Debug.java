package com.noreco1.fireflyv2.common;

import java.util.ArrayList;

public class Debug {

    public static void printAndQuit(ArrayList<Object> objs)
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (Object object : objs) {
            String val = String.valueOf(object);
            stringBuffer.append(val);
            stringBuffer.append("\n");
        }
        System.out.println(stringBuffer.toString());
        System.exit(0);
    }

    public static void printAndQuit(Object obj)
    {
        System.out.println(String.valueOf(obj));
        System.exit(0);
    }

    public static void print(Object obj)
    {
        System.out.println(String.valueOf(obj));
    }

    public static void print(ArrayList<Object> objs)
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (Object object : objs) {
            String val = String.valueOf(object);
            stringBuffer.append(val);
            stringBuffer.append("\n");
        }
        System.out.println(stringBuffer.toString());
    }

}
