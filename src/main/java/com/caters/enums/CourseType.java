package com.caters.enums;

public enum CourseType {
    WELCOME_DRINKS_AND_SNACKS("Welcome Drinks & Snacks"),
    MAIN_COURSE("Main Course"),
    DESSERTS("Desserts");

    private final String displayName;

    CourseType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}