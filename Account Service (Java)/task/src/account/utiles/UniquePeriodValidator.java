package account.utiles;

import account.dto.request.PaymentEmployeeRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniquePeriodValidator implements ConstraintValidator<UniquePeriod, List<PaymentEmployeeRequestDto>> {
    @Override
    public void initialize(UniquePeriod constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<PaymentEmployeeRequestDto> requestDtoList, ConstraintValidatorContext constraintValidatorContext) {
        Set<String> periods = new HashSet<>();
        for (PaymentEmployeeRequestDto dto : requestDtoList) {
            if (!periods.add(dto.getPeriod())) {
                return false; // Duplicate period found
            }
        }
        return true;
    }
}
