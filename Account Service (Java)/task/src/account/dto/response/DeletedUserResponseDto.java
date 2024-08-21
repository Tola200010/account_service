package account.dto.response;

public class DeletedUserResponseDto {
    private String user;
    private String status;

    public DeletedUserResponseDto(String user) {
        this.user = user;
        this.status = "Deleted successfully!";
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
