package com.yinfu.app.common;

public class Constants {

    public static final String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";

    public static class HealthRecordType {
        public static final String BLOOD_PRESSURE = "blood_pressure";
        public static final String HEART_RATE = "heart_rate";
        public static final String BLOOD_SUGAR = "blood_sugar";
        public static final String TEMPERATURE = "temperature";
    }

    public static class MedicationStatus {
        public static final String TAKEN = "taken";
        public static final String MISSED = "missed";
    }

    public static class SosStatus {
        public static final String TRIGGERED = "triggered";
        public static final String HANDLED = "handled";
        public static final String CANCELLED = "cancelled";
    }
}