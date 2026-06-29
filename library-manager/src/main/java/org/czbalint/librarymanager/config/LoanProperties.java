package org.czbalint.librarymanager.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "library.loan")
public class LoanProperties {

    private int periodDays = 14;

    private int maxActiveLoansPerReader = 5;

}
