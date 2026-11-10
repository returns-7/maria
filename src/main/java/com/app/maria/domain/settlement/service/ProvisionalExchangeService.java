package com.app.maria.domain.settlement.service;

import com.app.maria.domain.sellorder.dto.SellOrderDTO;

public interface ProvisionalExchangeService {
    void createProvisionalExchange(SellOrderDTO sellOrderDTO);
}
