package com.noreco1.fireflyv2.common.helpers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

public class SystemUtil {

    @Autowired
    Environment env;

    public SystemUtil(Environment env) {
        this.env = env;
    }

    public SystemUtil() {}

    public boolean inActiveProfiles(String profile) {
        String[] activeProfiles = env.getActiveProfiles();
        for(String activeProfile : activeProfiles) {
            if (activeProfile.equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
