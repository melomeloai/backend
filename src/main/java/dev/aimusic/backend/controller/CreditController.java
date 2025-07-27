package dev.aimusic.backend.controller;

import dev.aimusic.backend.auth.AuthenticationUtils;
import dev.aimusic.backend.credit.CreditService;
import dev.aimusic.backend.credit.dto.CreditInfoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.aimusic.backend.common.Constants.API_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = API_PATH + "/credits", produces = {APPLICATION_JSON_VALUE})
public class CreditController {

    private final CreditService creditService;

    /**
     * 获取用户积分信息
     * GET /api/credits
     */
    @GetMapping
    public ResponseEntity<CreditInfoResponse> getCreditInfo(Authentication auth) {
        var userId = AuthenticationUtils.getUserId(auth);
        var response = creditService.getUserCreditInfo(userId);
        return ResponseEntity.ok(response);
    }
}
