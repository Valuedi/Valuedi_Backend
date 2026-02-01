package org.umc.valuedi.domain.auth.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.umc.valuedi.domain.auth.dto.event.AuthMailEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthEmailEventListener {
    private final JavaMailSender mailSender;

    @Async("mailExecutor")
    @EventListener
    @Retryable(
            value = {MailException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void handleAuthEmailEvent(AuthMailEvent event) {
        log.info("메일 발송 시도 중... (Target: {})", event.email());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.email());
        message.setSubject("[Valuedi] 회원가입 인증번호입니다.");
        message.setText("인증번호는 [" + event.code() + "] 입니다. 3분 이내에 입력해 주세요.");

        mailSender.send(message);
        log.info("메일 발송 성공! (Target: {})", event.email());
    }


    @Recover
    public void recover(MailException e, AuthMailEvent event) {
        log.error("메일 발송 최종 실패. 모든 재시도 소진. (Target: {}, Error: {})",
                event.email(), e.getMessage());
    }
}
