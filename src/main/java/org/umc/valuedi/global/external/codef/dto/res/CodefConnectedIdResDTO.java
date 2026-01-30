package org.umc.valuedi.global.external.codef.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CodefConnectedIdResDTO {

    @JsonProperty("connectedId")
    private String connectedId;
}
