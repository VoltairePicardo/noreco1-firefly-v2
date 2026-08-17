package com.noreco1.fireflyv2.common.helpers;

import java.util.HashMap;
import java.util.Map;

public class Client {

    public static Map getClientInfo() {
        Map map = new HashMap();
        map.put("system_uname", "system_uname");
        map.put("hostname", "hostname");

        map.put("ip_address", "ip");

        map.put("mac_address", "mac");
        return map;
    }
}
