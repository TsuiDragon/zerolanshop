package cn.zerolan.zerolanshop.auth.service;

import java.util.Locale;

public enum SmsScene {
    REGISTER("REG"),
    LOGIN("LOGIN"),
    PASSWORD_RESET("RESET"),
    BIND_PHONE("BIND");

    private final String schemeSuffix;

    SmsScene(String schemeSuffix) {
        this.schemeSuffix = schemeSuffix;
    }

    public String getSchemeSuffix() {
        return schemeSuffix;
    }

    public static SmsScene from(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        for (SmsScene scene : values()) {
            if (scene.name().equals(normalized)) {
                return scene;
            }
        }
        throw new RuntimeException("不支持的短信验证场景");
    }
}
