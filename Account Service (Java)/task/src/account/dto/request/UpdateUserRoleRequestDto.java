package account.dto.request;

import account.utiles.UpdateRoleOperationType;
import jakarta.validation.constraints.NotBlank;

public class UpdateUserRoleRequestDto {
    @NotBlank
    private String user;
    @NotBlank
    private String role;
    private UpdateRoleOperationType operation;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UpdateRoleOperationType getOperation() {
        return operation;
    }

    public void setOperation( UpdateRoleOperationType operation) {
        this.operation = operation;
    }
}
