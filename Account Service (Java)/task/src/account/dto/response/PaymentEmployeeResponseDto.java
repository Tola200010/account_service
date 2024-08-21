package account.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentEmployeeResponseDto {
  private String name;
  @JsonProperty("lastname")
  private String lastName;
  private String period;
  private String salary;

    public PaymentEmployeeResponseDto(String name, String lastName, String period, String salary) {
        this.name = name;
        this.lastName = lastName;
        this.period = period;
        this.salary = salary;
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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }
}
