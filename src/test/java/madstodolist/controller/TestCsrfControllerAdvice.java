package madstodolist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// ControllerAdvice to add a mock CSRF token to the model during tests
@ControllerAdvice
public class TestCsrfControllerAdvice {

    @ModelAttribute
    public void addCsrfToken(org.springframework.ui.Model model) {
        model.addAttribute("_csrf", new MockCsrfToken());
    }

    // Inner class to mock CsrfToken
    public static class MockCsrfToken {
        public String getToken() {
            return "mockCsrfToken";
        }

        public String getHeaderName() {
            return "X-CSRF-TOKEN";
        }

        public String getParameterName() {
            return "_csrf";
        }
    }
}

