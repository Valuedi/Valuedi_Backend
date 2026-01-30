package org.umc.valuedi.domain.connection.dto.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.umc.valuedi.domain.connection.entity.CodefConnection;

@Getter
@RequiredArgsConstructor
public class ConnectionSuccessEvent {
    private final CodefConnection connection;
}
