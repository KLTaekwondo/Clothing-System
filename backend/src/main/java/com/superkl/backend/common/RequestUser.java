package com.superkl.backend.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestUser {
    private Long requestId;
    private String requestRole;
}
