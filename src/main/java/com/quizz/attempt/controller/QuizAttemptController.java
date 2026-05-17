package com.quizz.attempt.controller;

import com.quizz.attempt.dto.StartQuizResponse;
import com.quizz.attempt.dto.SubmitQuizRequest;
import com.quizz.attempt.dto.ResultChartResponse;
import com.quizz.attempt.entity.QuizAttempt;
import com.quizz.attempt.mapper.QuizAttemptMapper;
import com.quizz.attempt.service.QuizAttemptCommandService;
import com.quizz.attempt.service.QuizAttemptQueryService;
import com.quizz.security.context.CurrentUserProvider;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public String start(@RequestParam Long quizId) {
        Long userId = currentUserProvider.getCurrentUserId();
        StartQuizResponse response = quizAttemptCommandService.startAttempt(quizId, userId);
        return "redirect:/attempts/" + response.attemptId();
    }

    @GetMapping("/attempts/{attemptId}")
    public String play(@PathVariable Long attemptId, Model model) {
        Long userId = currentUserProvider.getCurrentUserId();
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

    @GetMapping("/attempts/{attemptId}/result")
    public String result(@PathVariable Long attemptId, Model model) {
        Long userId = currentUserProvider.getCurrentUserId();
        QuizAttempt attempt = quizAttemptQueryService.getResult(attemptId, userId);
        model.addAttribute("result", quizAttemptMapper.toResultResponse(attempt));
        return "attempt/result";
    }

    @GetMapping("/attempts/history")
    public String history(Model model) {
        Long userId = currentUserProvider.getCurrentUserId();
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
        return quizAttemptQueryService.getResultChart(attemptId, userId);
    }
}
