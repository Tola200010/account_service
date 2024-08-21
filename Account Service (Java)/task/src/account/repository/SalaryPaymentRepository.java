package account.repository;

import account.entities.SalaryPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment,Integer> {
    Optional<SalaryPayment> findByEmailAndPeriod(String email,String period);
    List<SalaryPayment> findByEmailOrderByPeriodDesc(String email);
    Optional<SalaryPayment> findByEmailAndPeriodOrderByPeriodDesc(String email,String period);
}
