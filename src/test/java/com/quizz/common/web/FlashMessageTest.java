package com.quizz.common.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlashMessageTest {

    @Test
    void successCreatesSuccessFlashMessage() {
        FlashMessage flashMessage = FlashMessage.success("x");

        assertThat(flashMessage.type()).isEqualTo("success");
        assertThat(flashMessage.text()).isEqualTo("x");
    }

    @Test
    void errorCreatesErrorFlashMessage() {
        FlashMessage flashMessage = FlashMessage.error("x");

        assertThat(flashMessage.type()).isEqualTo("error");
        assertThat(flashMessage.text()).isEqualTo("x");
    }

    @Test
    void warningCreatesWarningFlashMessage() {
        FlashMessage flashMessage = FlashMessage.warning("x");

        assertThat(flashMessage.type()).isEqualTo("warning");
        assertThat(flashMessage.text()).isEqualTo("x");
    }

    @Test
    void infoCreatesInfoFlashMessage() {
        FlashMessage flashMessage = FlashMessage.info("x");

        assertThat(flashMessage.type()).isEqualTo("info");
        assertThat(flashMessage.text()).isEqualTo("x");
    }
}
