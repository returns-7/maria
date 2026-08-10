package com.app.maria.domain.sellorder.service;

import com.app.maria.domain.sellorder.dto.request.SellOrderRequestDTO;
import com.app.maria.domain.sellorder.dto.response.SellOrderResponseDTO;

import java.util.List;

public interface SellOrderService {

    List<SellOrderResponseDTO> placeSellOrder(Long actorAdminId,SellOrderRequestDTO request);
    SellOrderResponseDTO getSellOrder(Long orderId);
    List<SellOrderResponseDTO> getSellOrderByAccount(Long accountId);

}
