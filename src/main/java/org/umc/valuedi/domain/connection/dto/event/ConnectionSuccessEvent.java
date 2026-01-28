package org.umc.valuedi.domain.connection.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ConnectionSuccessEvent {
    private final Long connectionId;
}
