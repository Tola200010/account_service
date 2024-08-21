package account.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class SignUpResponseDTO {
    private Integer id;
    private String name;
    @JsonProperty("lastname")
    private String lastName;
    private String email;
    private List<String> roles;

    public SignUpResponseDTO(Integer id, String name, String lastName, String email,List<String> roles) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.roles = roles;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
