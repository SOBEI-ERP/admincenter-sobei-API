package br.org.sobei.denuncias.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;
    private UserInfo user;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfo {
        private String usuario;
        private String nivel;
    }
}
