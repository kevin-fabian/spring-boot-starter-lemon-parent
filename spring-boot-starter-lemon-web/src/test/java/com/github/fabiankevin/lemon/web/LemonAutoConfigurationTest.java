package com.github.fabiankevin.lemon.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LemonAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LemonAutoConfiguration.class));

    @Test
    void globalExceptionHandler_shouldHasSingleBean() {
        this.contextRunner.withUserConfiguration(LemonAutoConfiguration.class).run((context) -> {
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
        });
    }

}
