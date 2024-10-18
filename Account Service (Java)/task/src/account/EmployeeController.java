package account;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.stream.Stream;

@RestController
@RequestMapping("api/empl")
public class EmployeeController {
    private final PayrollRepository payrollRepository;

    EmployeeController(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @GetMapping(value = "payment", params = "period")
    public PaymentResponse getPayroll(@RequestParam @DateTimeFormat(pattern = "MM-yyyy") @NotNull YearMonth period, @AuthenticationPrincipal User user) {
        Payroll p = payrollRepository.findByUserAndPeriod(user, period);
        return new PaymentResponse(user, p);
    }

    @GetMapping(value = "payment", params = "!period")
    @Transactional
    public Stream<PaymentResponse> getPayrolls(@AuthenticationPrincipal User user) {
        return payrollRepository.findByUserOrderByPeriodDesc(user).map(p -> new PaymentResponse(user, p));
    }

    record PaymentResponse(String name, String lastname, @JsonFormat(pattern = "MMMM-yyyy") YearMonth period,
                           String salary) {
        PaymentResponse(User user, Payroll payroll) {
            this(user.name, user.lastname, payroll.period, "%d dollar(s) %d cent(s)".formatted(payroll.salary / 100, payroll.salary % 100));
        }
    }
}
