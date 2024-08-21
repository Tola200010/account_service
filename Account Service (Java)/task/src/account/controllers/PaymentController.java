package account.controllers;

import account.dto.request.PaymentEmployeeRequestDto;
import account.dto.response.PaymentEmployeeResponseDto;
import account.dto.response.StatusResponseDto;
import account.entities.AppUser;
import account.entities.SalaryPayment;
import account.exception.BadRequestException;
import account.exception.EmployeeNotFoundException;
import account.repository.AppUserRepository;
import account.repository.SalaryPaymentRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormatSymbols;
import java.util.*;

@RestController
@Validated
@RequestMapping("/api")
public class PaymentController {
    private final AppUserRepository appUserRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;

    public PaymentController(AppUserRepository appUserRepository, SalaryPaymentRepository salaryPaymentRepository) {
        this.appUserRepository = appUserRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<?> getEmployee(
            @RequestParam(name = "period",required = false)
            @Pattern(regexp = "^(0[1-9]|1[0-2])-(\\d{4})$", message = "Invalid period format. It must be MM-yyyy.")
            String period,
            @AuthenticationPrincipal UserDetails userDetails) {
        AppUser appUser = appUserRepository.findByEmail(userDetails.getUsername()).orElse(null);
        // user never null, this endpoint available only authentication user
        assert appUser != null;
        if(period == null){
            List<SalaryPayment> salaryPaymentList = salaryPaymentRepository.findByEmailOrderByPeriodDesc(userDetails.getUsername().toLowerCase());
            List<PaymentEmployeeResponseDto> paymentEmployeeResponseDtos = new ArrayList<>();
            salaryPaymentList.forEach(x -> {
                String[] periodParts = x.getPeriod().split("-");
                String month = convertMonth(periodParts[0]);
                String year = periodParts[1];
                String formattedPeriod = month + "-" + year;
                PaymentEmployeeResponseDto paymentEmployeeResponseDto = new PaymentEmployeeResponseDto(
                        appUser.getName(),
                        appUser.getLastname(),
                        formattedPeriod,
                        formatSalary(x.getSalary())
                );
                paymentEmployeeResponseDtos.add(paymentEmployeeResponseDto);
            });
            return new ResponseEntity<>(paymentEmployeeResponseDtos,HttpStatus.OK);
        }else{
            SalaryPayment salaryPayment = salaryPaymentRepository.findByEmailAndPeriodOrderByPeriodDesc(userDetails.getUsername().toLowerCase(), period).orElse(null);
            if(salaryPayment == null){
                return new ResponseEntity<>(List.of(),HttpStatus.OK);
            }
            String[] periodParts = salaryPayment.getPeriod().split("-");
            String month = convertMonth(periodParts[0]);
            String year = periodParts[1];
            String formattedPeriod = month + "-" + year;
            PaymentEmployeeResponseDto paymentEmployeeResponseDto = new PaymentEmployeeResponseDto(
                    appUser.getName(),
                    appUser.getLastname(),
                    formattedPeriod,
                    formatSalary(salaryPayment.getSalary())
            );
            return new ResponseEntity<>(paymentEmployeeResponseDto,HttpStatus.OK);
        }
    }
    private static String formatSalary(Long salaryInCents) {
        long dollars = salaryInCents / 100;
        long cents = salaryInCents % 100;
        return dollars + " dollar(s) " + cents + " cent(s)";
    }
    private static String convertMonth(String monthNumber) {
        int month = Integer.parseInt(monthNumber);
        return new DateFormatSymbols().getMonths()[month - 1];
    }
    @PostMapping("/acct/payments")
    @Transactional
    public ResponseEntity<StatusResponseDto> registerEmpPayment(@RequestBody List<@Valid PaymentEmployeeRequestDto> requestDtoList) {
        if(!isValidPeriod(requestDtoList)){
            throw new BadRequestException("The value request is not matched");
        }
        List<SalaryPayment> salaryPaymentList = new ArrayList<>();
        requestDtoList.stream().parallel().forEach(x -> {
            appUserRepository.findByEmail(x.getEmployee().toLowerCase()).orElseThrow(EmployeeNotFoundException::new);
            SalaryPayment salaryPayment = new SalaryPayment();
            salaryPayment.setEmail(x.getEmployee().toLowerCase());
            salaryPayment.setPeriod(x.getPeriod());
            salaryPayment.setSalary(x.getSalary());
            salaryPaymentList.add(salaryPayment);
        });
        salaryPaymentRepository.saveAll(salaryPaymentList);
        return new ResponseEntity<>(new StatusResponseDto("Added successfully!"), HttpStatus.OK);
    }

    private boolean isValidPeriod(List<PaymentEmployeeRequestDto> paymentEmployeeRequestDtos){
        Set<String> periods = new HashSet<>();
        for (PaymentEmployeeRequestDto dto : paymentEmployeeRequestDtos) {
            String periodEmail = dto.getPeriod() + "|" + dto.getEmployee();
            if (!periods.add(periodEmail)) {
                return false; // Duplicate combination of period and email found
            }
        }
        return true;
    }

    @PutMapping("/acct/payments")
    public ResponseEntity<StatusResponseDto> updateEmpPayment(@RequestBody @Valid PaymentEmployeeRequestDto employeeRequestDto) {
        SalaryPayment salaryPayment = salaryPaymentRepository.findByEmailAndPeriod(employeeRequestDto.getEmployee().toLowerCase(), employeeRequestDto.getPeriod()).orElseThrow(EmployeeNotFoundException::new);
        salaryPayment.setSalary(employeeRequestDto.getSalary());
        salaryPaymentRepository.save(salaryPayment);
        return new ResponseEntity<>(new StatusResponseDto("Updated successfully!"), HttpStatus.OK);
    }
}
