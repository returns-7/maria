package com.app.maria.domain.account.provider;

import com.app.maria.domain.account.dto.AccountDTO;
import com.app.maria.domain.account.provider.dto.response.MydataRiaAccountsResponseDTO;
import java.math.BigDecimal;
import org.springframework.http.HttpStatusCode;

public interface MydataProvider {
    BigDecimal getExternalConfiguredLimit(String ciHash);

    MydataRiaAccountsResponseDTO getRiaAccounts(String ciHash);

    HttpStatusCode syncRiaAccount(String ciHash, AccountDTO account);
}
