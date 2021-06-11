package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslValueDiscountOrSurchargeCommand implements Command {
    private final Boolean isDiscount;
    private final Long amount;

    private IslValueDiscountOrSurchargeCommand(Boolean isDiscount, Long amount) {
        this.isDiscount = isDiscount;
        this.amount = amount;
    }

    public static IslValueDiscountOrSurchargeCommand fromString(String inputString) {
        long amount = Long.parseLong(inputString.substring(1));
        if(amount > 99999999) throw new RuntimeException("PRICE OUT OF BOUNDS");
        return new IslValueDiscountOrSurchargeCommand(
                Boolean.valueOf(inputString.substring(0,1)),
                amount
        );
    }

    public Boolean getDiscount() {
        return isDiscount;
    }

    public Long getAmount() {
        return amount;
    }
}
