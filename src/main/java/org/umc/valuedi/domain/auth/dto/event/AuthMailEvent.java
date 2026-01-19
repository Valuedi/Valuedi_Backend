package org.umc.valuedi.domain.auth.dto.event;

public record AuthMailEvent(
        String email,
        String code
) {
}
