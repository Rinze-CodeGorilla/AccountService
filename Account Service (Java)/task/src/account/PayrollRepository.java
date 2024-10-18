package account;


import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.stream.Stream;

public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Payroll findByUserAndPeriod(User user, YearMonth period);

    Stream<Payroll> findByUserOrderByPeriodDesc(User user);
}
