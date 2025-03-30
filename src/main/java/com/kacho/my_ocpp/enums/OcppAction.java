package com.kacho.my_ocpp.enums;

import com.kacho.my_ocpp.exception.CallErrorException;

import java.util.*;

public enum OcppAction {

    BOOT_NOTIFICATION("BootNotification"),
    HEARTBEAT("Heartbeat"),
    STATUS_NOTIFICATION("StatusNotification"),
    START_TRANSACTION("StartTransaction"),
    STOP_TRANSACTION("StopTransaction");


    private static final Map<String, OcppAction> ACTION_MAP = new HashMap<>();
    private static final List<String> ACTION_NAMES = new ArrayList<>();

    static {
        for(OcppAction action : values()) {
            ACTION_MAP.put(action.getActionName(), action);
            ACTION_NAMES.add(action.getActionName());
        }
    }

    private final String actionName;

    OcppAction(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return this.actionName;
    }

    public static Map<String, OcppAction> getActionMap() {
        return ACTION_MAP;
    }

    public static List<String> getActionNames() {
        return ACTION_NAMES;
    }
}
