package com.app.maria.presentation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

class PageControllerTest {

    @Test
    void withdrawalManagementPageUsesWithdrawalTemplateAndActivePath() {
        ConcurrentModel model = new ConcurrentModel();

        String viewName = new PageController().withdrawals(model);

        assertThat(viewName).isEqualTo("withdrawals");
        assertThat(model.getAttribute("activePath")).isEqualTo("/admin/withdrawals");
    }
}
