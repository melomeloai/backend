package dev.aimusic.backend.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class AbstractResponse {
    private RequestStatus requestStatus;
}
