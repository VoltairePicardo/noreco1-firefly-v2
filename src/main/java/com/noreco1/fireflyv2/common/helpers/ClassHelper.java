package com.noreco1.fireflyv2.common.helpers;

import com.noreco1.fireflyv2.model.User;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

@Slf4j
public class ClassHelper {

    public static void setSignatoryValue(Object documentObj, String classFullPath, String property, User signer) {
        try {
            Class<?> docClazz = Class.forName(classFullPath);
            do {
                try {
                    Field field = docClazz.getDeclaredField(property); // e.g. FK_checkedByUserId
                    field.setAccessible(true);
                    field.set(documentObj, signer);

                } catch(NoSuchFieldException e) {}
            } while((docClazz = docClazz.getSuperclass()) != null);  // to facilitate inheritance

        } catch (ClassNotFoundException e) {
            log.error("ClassHelper.setSignatoryValue: class not found - {}", classFullPath, e);
        } catch (IllegalAccessException e) {
            log.error("ClassHelper.setSignatoryValue: failed to set property '{}' on {}", property, classFullPath, e);
        }
    }
}
