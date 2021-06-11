package com.viamindsoft.vfp;


import java.util.Map;

public interface FiscalPrinterData {
    String serialNumber();
    String fiscalMemoryNum();
    String fiscalPrinterModel();
    long lastReceipt();
    long lastInvoiceNum();
    long invoiceStart();
    long invoiceEnd();
    String errorCode();
    boolean hasError();
    void setError(String error);
    void incrementInvoice();
    void incrementLastReceipt();
    void setInvoiceRange(long start, long end);
    Map<Integer,Long> paymentsTotals();
    void clearTotals();
    void updateTotals(Map<Integer,Long> payments);
    void addTotal(int paymentType, Long amount);
    boolean attemptToClearErrors();
}
