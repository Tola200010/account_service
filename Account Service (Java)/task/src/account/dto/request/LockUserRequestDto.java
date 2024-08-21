package account.dto.request;

import account.utiles.LockUserOperation;
import account.utiles.UpdateRoleOperationType;
import jakarta.validation.constraints.NotBlank;

public class LockUserRequestDto {
    @NotBlank
    private String user;
    private LockUserOperation operation;

    public @NotBlank String getUser() {
        return user;
    }

    public void setUser(@NotBlank String user) {
        this.user = user;
    }

    public LockUserOperation getOperation() {
        return operation;
    }

    public void setOperation(LockUserOperation operation) {
        this.operation = operation;
    }
}
