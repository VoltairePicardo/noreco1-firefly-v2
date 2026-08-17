package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.common.GlobalConstant;
import com.noreco1.fireflyv2.model.User;

import java.lang.reflect.Field;

public class ClassHelper {

    public static void setSignatoryValue(Object documentObj, String classFullPath, String property, User signer) {
        try {
            Class<?> docClazz = Class.forName(classFullPath);
            Object instance = docClazz.newInstance();

            Class<?> newDocClazz = instance.getClass();
            if (newDocClazz != null) {
                do {
                    try {
                        Field field = newDocClazz.getDeclaredField(property); // e.g. FK_checkedByUserId
                        field.setAccessible(true);
                        field.set(documentObj, signer);

                    } catch(NoSuchFieldException e) {}
                }while((newDocClazz = newDocClazz.getSuperclass()) != null);  // to facilitate inheritance
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
