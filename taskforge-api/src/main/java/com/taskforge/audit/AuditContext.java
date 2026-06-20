package com.taskforge.audit;

public final class AuditContext {

    private static final ThreadLocal<Object> OLD_VALUE = new ThreadLocal<>();

    private AuditContext() {
    }

    public static void setOldValue(Object value) {
        OLD_VALUE.set(value);
    }

    public static Object getOldValue() {
        return OLD_VALUE.get();
    }

    public static void clear() {
        OLD_VALUE.remove();
    }
}
