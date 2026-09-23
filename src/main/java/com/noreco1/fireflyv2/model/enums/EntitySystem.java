package com.noreco1.fireflyv2.model.enums;

public enum EntitySystem {

    NORECO1_IBCMS_MSSQL("NORECO1_IBCMS_MSSQL", "IBCMS Primary MSSQL Database"),
    NORECO1_MYSQL_FIREFLY("NORECO1_MYSQL_FIREFLY", "MySQL Firefly Database (legacy)"),
    NORECO1_FIREFLY_V2("NORECO1_FIREFLY_V2", "Firefly v2 System");

    private final String code;
    private final String description;

    EntitySystem(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static EntitySystem fromCode(String code) {
        if (code == null) return null;
        for (EntitySystem sys : values()) {
            if (sys.code.equalsIgnoreCase(code)) return sys;
        }
        throw new IllegalArgumentException("Unknown entity system: " + code);
    }
}
