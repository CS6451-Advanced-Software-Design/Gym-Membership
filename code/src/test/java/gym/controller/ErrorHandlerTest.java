package gym.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gym.domain.NotFoundException;
import gym.domain.RuleViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class ErrorHandlerTest {

    @RestController
    static class FailingController {

        @GetMapping("/missing")
        void missing() {
            throw new NotFoundException("Member", "42");
        }

        @GetMapping("/frozen")
        void frozen() {
            throw new RuleViolationException("Membership is frozen");
        }
    }

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new FailingController())
            .setControllerAdvice(new ErrorHandler())
            .build();

    @Test
    void notFoundBecomes404() throws Exception {
        mvc.perform(get("/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Member 42 not found"));
    }

    @Test
    void ruleViolationBecomes422() throws Exception {
        mvc.perform(get("/frozen"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Membership is frozen"));
    }
}
