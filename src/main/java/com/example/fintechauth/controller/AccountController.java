package com.example.fintechauth.controller;

import com.example.fintechauth.dto.AccountResponse;
import com.example.fintechauth.entity.Account;
import com.example.fintechauth.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@CrossOrigin(origins = "http://localhost:3000")
public class AccountController {

    private final AccountRepository accountRepository;

    public AccountController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // 🔐 패치된(안전한) 버전: 계좌 소유자 검증 포함
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id,
            Authentication authentication   // 현재 로그인한 사용자 정보
    ) {
        // 1. 현재 로그인한 사용자 이메일(= JWT subject) 가져오기
        String currentEmail = authentication.getName();

        // 2. 요청된 계좌 조회
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다."));

        // 3. 계좌 소유자와 현재 로그인 사용자 비교 (IDOR 방어 핵심)
        if (!account.getOwner().getEmail().equals(currentEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // 4. 본인 계좌일 때만 응답
        AccountResponse response = new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getBalance(),
                account.getOwner().getEmail()
        );

        return ResponseEntity.ok(response);
    }
}
