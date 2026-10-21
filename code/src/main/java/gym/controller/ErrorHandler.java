package gym.controller;

import gym.domain.NotFoundException;
import gym.domain.RuleViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Turns domain exceptions into RFC 9457 problem responses for every controller.
 * Spring's own errors, such as a failed {@code @Valid} check, become 400 problems through the base class.
 */
@RestControllerAdvice
public class ErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail notFound(NotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(RuleViolationException.class)
    public ProblemDetail ruleViolation(RuleViolationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, exception.getMessage());
    }
}
