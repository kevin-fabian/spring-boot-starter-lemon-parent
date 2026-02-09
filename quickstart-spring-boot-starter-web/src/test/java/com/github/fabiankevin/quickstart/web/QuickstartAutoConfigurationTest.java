package com.github.fabiankevin.quickstart.web;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class QuickstartAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(QuickstartAutoConfiguration.class));

    @Test
    void globalExceptionHandler_shouldHasSingleBean() {
        this.contextRunner.withUserConfiguration(QuickstartAutoConfiguration.class).run((context) -> {
            assertThat(context).hasSingleBean(GlobalExceptionHandler.class);
        });
    }

}
