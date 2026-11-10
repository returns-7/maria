package com.app.maria.domain.customer.dto.response;

import com.app.maria.domain.customer.dto.CustomerSearchDTO;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public class CustomerSearchResponseDTO {

    private final Long customerId;
    private final String name;
    private final LocalDate birthDate;
    private final String maskedPhone;
    private final String investorType;

    public CustomerSearchResponseDTO(CustomerSearchDTO customer) {
        this.customerId = customer.getCustomerId();
        this.name = customer.getName();
        this.birthDate = customer.getBirthDate();
        this.maskedPhone = maskPhone(customer.getPhone());
        this.investorType = customer.getInvestorType();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "-";
        }

        Matcher matcher =
                Pattern.compile("^(\\d{2,3})-?(\\d{3,4})-?(\\d{4})$").matcher(phone.trim());

        if (!matcher.matches()) {
            return "****";
        }

        return matcher.group(1) + "-****-" + matcher.group(3);
    }
}
