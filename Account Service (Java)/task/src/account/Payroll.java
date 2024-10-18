package account;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.sql.Date;
import java.time.YearMonth;

@Entity
@Table(name = "payroll", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "period"}))
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    User user;
    @Convert(converter = YearMonthDateAttributeConverter.class)
    YearMonth period;
    @Positive
    long salary;

    Payroll(User user, YearMonth period, long salary) {
        this.user = user;
        this.period = period;
        this.salary = salary;
    }

    public Payroll() {

    }

    static class YearMonthDateAttributeConverter implements AttributeConverter<YearMonth, Date> {

        @Override
        public Date convertToDatabaseColumn(YearMonth attribute) {
            if (attribute == null) return null;
            return Date.valueOf(attribute.atDay(1));
        }

        @Override
        public YearMonth convertToEntityAttribute(Date dbData) {
            if (dbData == null) return null;
            return YearMonth.from(dbData.toLocalDate());
        }
    }
}