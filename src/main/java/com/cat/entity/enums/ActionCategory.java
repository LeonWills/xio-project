package com.cat.entity.enums;

public enum ActionCategory {
    /**
     * 取板动作。
     */
    PICK("取板"),
    /**
     * 旋转动作。
     */
    ROTATE("旋转"),
    /**
     * 进刀动作。
     */
    CUT("进刀"),
    /**
     * 送板动作。
     */
    SEND("送板");

    public final String value;

    ActionCategory(String value) {
        this.value = value;
    }
}
