package account.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class PaymentEmployeeRequestDto {
    @Pattern(regexp = "^[\\w\\.-]+@acme\\.com$", message = "Email must be a valid acme.com email address")
    private String employee;
    @Pattern(regexp = "^(0[1-9]|1[0-2])-(\\d{4})$", message = "Invalid period format. It must be MM-yyyy.")
    private String period;
    @Min(value = 0, message = "Amount cannot be negative")
    private Long salary;

    public @Pattern(regexp = "^[\\w\\.-]+@acme\\.com$", message = "Email must be a valid acme.com email address") String getEmployee() {
        return employee;
    }

    public void setEmployee(@Pattern(regexp = "^[\\w\\.-]+@acme\\.com$", message = "Email must be a valid acme.com email address") String employee) {
        this.employee = employee;
    }

    public @Pattern(regexp = "^(0[1-9]|1[0-2])-(\\d{4})$", message = "Invalid period format. It must be MM-yyyy.") String getPeriod() {
        return period;
    }

    public void setPeriod(@Pattern(regexp = "^(0[1-9]|1[0-2])-(\\d{4})$", message = "Invalid period format. It must be MM-yyyy.") String period) {
        this.period = period;
    }

    public @Min(value = 0, message = "Amount cannot be negative") Long getSalary() {
        return salary;
    }

    public void setSalary(@Min(value = 0, message = "Amount cannot be negative") Long salary) {
        this.salary = salary;
    }
}
