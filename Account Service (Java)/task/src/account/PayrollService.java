package account;

import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
public class PayrollService {
    private final PayrollRepository payrollRepository;
    private final UserRepository userRepository;

    PayrollService(PayrollRepository payrollRepository, UserRepository userRepository) {
        this.payrollRepository = payrollRepository;
        this.userRepository = userRepository;
    }

    Payroll addPayroll(String email, YearMonth period, long amount) {
        User u = userRepository.findByEmailIgnoreCase(email);
        Payroll p = new Payroll(u, period, amount);
        return payrollRepository.save(p);
    }

    Payroll updatePayroll(String email, YearMonth period, long salary) {
        User u = userRepository.findByEmailIgnoreCase(email);
        if (u == null) { throw new UserDetailsService.NotFoundException("User not found"); }
        Payroll p = payrollRepository.findByUserAndPeriod(u, period);
        if (p == null) { throw new UserDetailsService.NotFoundException("Salary not found"); }
        p.salary = salary;
        return payrollRepository.save(p);
    }
}
