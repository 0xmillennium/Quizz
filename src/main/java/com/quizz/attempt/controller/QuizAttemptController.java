package com.quizz.attempt.controller;

import com.quizz.attempt.dto.AutoSubmitResponse;
import com.quizz.attempt.dto.AutosaveAnswerRequest;
import com.quizz.attempt.dto.AutosaveAnswerResponse;
import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.mapper.QuizAttemptMapper;
import com.quizz.attempt.service.QuizAttemptCommandService;
import com.quizz.attempt.service.QuizAttemptQueryService;
import com.quizz.common.exception.BusinessRuleException;
import com.quizz.common.web.FlashMessage;
import com.quizz.security.context.CurrentUserProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Authenticated MVC boundary for taking, autosaving, submitting, and reviewing attempts.
 *
 * <p>The controller performs HTTP binding, current-user resolution, JSON/view
 * response selection, and redirect/flash behavior. Attempt lifecycle mutations
 * are delegated to {@link QuizAttemptCommandService}; play, result, chart, and
 * history reads are delegated to {@link QuizAttemptQueryService}.</p>
 */
@Controller
public class QuizAttemptController {

    private final CurrentUserProvider currentUserProvider;
    private final QuizAttemptCommandService quizAttemptCommandService;
    private final QuizAttemptQueryService quizAttemptQueryService;
    private final QuizAttemptMapper quizAttemptMapper;

    public QuizAttemptController(
            CurrentUserProvider currentUserProvider,
            QuizAttemptCommandService quizAttemptCommandService,
            QuizAttemptQueryService quizAttemptQueryService,
            QuizAttemptMapper quizAttemptMapper
    ) {
        this.currentUserProvider = currentUserProvider;
        this.quizAttemptCommandService = quizAttemptCommandService;
        this.quizAttemptQueryService = quizAttemptQueryService;
        this.quizAttemptMapper = quizAttemptMapper;
    }

    @PostMapping("/attempts/start")
    public String start(@RequestParam Long quizId, RedirectAttributes redirectAttributes) {
        Long userId = currentUserProvider.getCurrentUserId();
        StartQuizResponse response = quizAttemptCommandService.startAttempt(quizId, userId);
        redirectAttributes.addFlashAttribute("flashMessage", startFlashMessage(response));
        return "redirect:/attempts/" + response.attemptId();
    }

    @PostMapping("/attempts/{attemptId}/restart")
    public String restart(@PathVariable Long attemptId, RedirectAttributes redirectAttributes) {
        Long userId = currentUserProvider.getCurrentUserId();
        StartQuizResponse response = quizAttemptCommandService.restartAttempt(attemptId, userId);
        redirectAttributes.addFlashAttribute("flashMessage", response.previousAttemptAutoSubmitted()
                ? FlashMessage.info("Your previous attempt was automatically submitted because time expired. A new attempt has started.")
                : FlashMessage.success("Quiz restarted."));
        return "redirect:/attempts/" + response.attemptId();
    }

    @GetMapping("/attempts/{attemptId}")
    public String play(@PathVariable Long attemptId, Model model) {
        Long userId = currentUserProvider.getCurrentUserId();
        AutoSubmitResponse autoSubmitResponse = autoSubmitBoundary(attemptId, userId);
        if (autoSubmitResponse != null && "COMPLETED".equals(autoSubmitResponse.status())) {
            return "redirect:" + autoSubmitResponse.redirectUrl();
        }
        QuizAttempt attempt = quizAttemptQueryService.getAttemptPage(attemptId, userId);
        model.addAttribute("attempt", quizAttemptMapper.toAttemptPageResponse(attempt));
        model.addAttribute("submitQuizRequest", quizAttemptMapper.toSubmitQuizRequest(attempt));
        return "attempt/play";
    }

    @PostMapping("/attempts/{attemptId}/submit")
    public String submit(
            @PathVariable Long attemptId,
            @ModelAttribute("submitQuizRequest") SubmitQuizRequest request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        quizAttemptCommandService.submitAttempt(attemptId, userId, request);
        return "redirect:/attempts/" + attemptId + "/result";
    }

    @PostMapping("/attempts/{attemptId}/questions/{attemptQuestionId}/answer")
    @ResponseBody
    public AutosaveAnswerResponse autosaveAnswer(
            @PathVariable Long attemptId,
            @PathVariable Long attemptQuestionId,
            @ModelAttribute AutosaveAnswerRequest request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        if (request.getAnswerRevision() == null) {
            throw new com.quizz.common.exception.BusinessRuleException("Answer revision is required.");
        }
        return quizAttemptCommandService.autosaveAnswer(
                attemptId,
                attemptQuestionId,
                userId,
                request.getSelectedOptionId(),
                request.getAnswerRevision()
        );
    }

    @PostMapping("/attempts/{attemptId}/auto-submit")
    @ResponseBody
    public AutoSubmitResponse autoSubmit(@PathVariable Long attemptId) {
        Long userId = currentUserProvider.getCurrentUserId();
        return quizAttemptCommandService.autoSubmitIfOverdue(attemptId, userId);
    }

    @GetMapping("/attempts/{attemptId}/result")
    public String result(@PathVariable Long attemptId, Model model) {
        Long userId = currentUserProvider.getCurrentUserId();
        autoSubmitBoundary(attemptId, userId);
        QuizAttempt attempt = quizAttemptQueryService.getResult(attemptId, userId);
        model.addAttribute("result", quizAttemptMapper.toResultResponse(attempt));
        return "attempt/result";
    }

    @GetMapping("/attempts/history")
    public String history(Model model) {
        Long userId = currentUserProvider.getCurrentUserId();
        quizAttemptCommandService.autoSubmitOverdueAttemptsForUser(userId);
        model.addAttribute(
                "history",
                quizAttemptMapper.toHistoryResponseList(quizAttemptQueryService.findHistoryByUser(userId))
        );
        return "attempt/history";
    }

    @GetMapping("/attempts/{attemptId}/chart-data")
    @ResponseBody
    public ResultChartResponse chartData(@PathVariable Long attemptId) {
        Long userId = currentUserProvider.getCurrentUserId();
        autoSubmitBoundary(attemptId, userId);
        return quizAttemptQueryService.getResultChart(attemptId, userId);
    }

    private AutoSubmitResponse autoSubmitBoundary(Long attemptId, Long userId) {
        try {
            return quizAttemptCommandService.autoSubmitIfOverdue(attemptId, userId);
        } catch (BusinessRuleException exception) {
            if ("Attempt has not expired yet.".equals(exception.getMessage())) {
                return null;
            }
            throw exception;
        }
    }

    private FlashMessage startFlashMessage(StartQuizResponse response) {
        if (response.resumed()) {
            return FlashMessage.info("Continuing your active attempt.");
        }
        if (response.previousAttemptAutoSubmitted()) {
            return FlashMessage.info("Your previous attempt was automatically submitted because time expired. A new attempt has started.");
        }
        return FlashMessage.success("Quiz started.");
    }
}
