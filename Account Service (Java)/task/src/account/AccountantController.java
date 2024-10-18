package account;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/acct")
@Validated
public class AccountantController {
    private final PayrollService payrollService;

    AccountantController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("payments")
    @Transactional
    UploadPayrollResponse uploadPayroll(@RequestBody List<@Valid PayrollRequest> payrollRequests) {
        try {
            for (PayrollRequest payrollRequest : payrollRequests) {
                payrollService.addPayroll(payrollRequest.employee, payrollRequest.period, payrollRequest.salary);
            }
        } catch (DataIntegrityViolationException e) {
            throw new DuplicatePayrollException();
        } catch (ConstraintViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new UploadPayrollResponse();
    }

    @PutMapping("payments")
    UpdatePayrollResponse changeSalary(@RequestBody @Valid PayrollRequest payrollRequest) {
        try {
            payrollService.updatePayroll(payrollRequest.employee, payrollRequest.period, payrollRequest.salary);
        } catch (ConstraintViolationException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new UpdatePayrollResponse();
    }

    /*
    This exists because in this version of Spring Boot Constraint Violations in Lists aren't handled in the same way as Constraint Violations in not-Lists.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    void handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    record PayrollRequest(@Email(regexp = ".+@acme.com") @NotNull String employee,
                          @JsonFormat(pattern = "MM-yyyy") @NotNull YearMonth period, @Positive long salary) {
    }

    record UploadPayrollResponse(String status) {
        UploadPayrollResponse() {
            this("Added successfully!");
        }
    }

    record UpdatePayrollResponse(String status) {
        UpdatePayrollResponse() {
            this("Updated successfully!");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    static class DuplicatePayrollException extends RuntimeException {
        DuplicatePayrollException() {
            super("Payroll already exists!");
        }
    }
}
