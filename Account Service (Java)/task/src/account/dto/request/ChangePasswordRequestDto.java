package account.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangePasswordRequestDto {
    @JsonProperty("new_password")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
