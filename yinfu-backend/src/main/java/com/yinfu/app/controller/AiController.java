package com.yinfu.app.controller;

import com.yinfu.app.common.Constants;
import com.yinfu.app.common.JwtUtil;
import com.yinfu.app.common.Result;
import com.yinfu.app.dto.AiChatRequest;
import com.yinfu.app.dto.AiDiagnosisRequest;
import com.yinfu.app.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/chat")
    public Result<?> chat(HttpServletRequest request, @RequestBody AiChatRequest chatRequest) {
        return aiService.chat(chatRequest);
    }

    @PostMapping("/diagnosis")
    public Result<?> diagnosis(HttpServletRequest request, @RequestBody AiDiagnosisRequest diagnosisRequest) {
        return aiService.diagnosis(diagnosisRequest);
    }
}