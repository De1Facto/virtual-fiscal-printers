package com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl.*;
import serialDevicesMock.FiscalPrinters.Ds.Commands.isl.*;
import com.viamindsoft.vfp.FiscalPrinters.Ds.FiscalReceipts.CurrentFiscalReceipt;

public interface IslCurrentFiscalReceipt extends CurrentFiscalReceipt {
    void addItem(IslItemSaleCommand addItemCommand);
    void addPayment(IslPaymentAndFinishCommand addPaymentCommand);
    void attemptVoid(IslVoidCommand voidCommand);

    void addDiscountOrSurcharge(IslPercentDiscountOrSurcharge addPercentDiscountOrSurcharge);
    void addDiscountOrSurcharge(IslValueDiscountOrSurchargeCommand addValueDiscountOrSurcharge);
    void subtotal(IslSubtotalCommand command);
    void addComment(IslPrintCommentCommand commentCommand);
}
