package com.app.maria.domain.settlement.exception;

public class ExchangeRateExternalApiException extends SettlementException {
  public ExchangeRateExternalApiException(String message, Throwable cause) {
    super(message);
    if (cause != null) {
      initCause(cause);
    }
  }
}
