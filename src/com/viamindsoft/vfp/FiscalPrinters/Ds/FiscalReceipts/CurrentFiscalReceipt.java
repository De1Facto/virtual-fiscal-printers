package com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

import java.util.Map;

public interface CurrentFiscalReceipt {
    Long documentNumber();
    String uniqueSaleNumber();
    Long amount();
    Map<Integer,Long> paymentsTotals();
    Boolean isReversal();
    Boolean isFinished();
    void openReceipt(Command openReceiptCommand);
    void addPayment(Command addPaymentCommand);
    void addItem(Command addItemCommand);
    void addDiscountOrSurcharge(Command addValueDiscountOrSurcharge);
    void attemptVoid(Command voidCommand);
    void subtotal(Command subtotal);
    void addComment(Command addComment);
}
