package com.quizz.common.web;

public record FlashMessage(String type, String text) {

    public static FlashMessage success(String text) {
        return new FlashMessage("success", text);
    }

    public static FlashMessage error(String text) {
        return new FlashMessage("error", text);
    }

    public static FlashMessage warning(String text) {
        return new FlashMessage("warning", text);
    }

    public static FlashMessage info(String text) {
        return new FlashMessage("info", text);
    }
}
