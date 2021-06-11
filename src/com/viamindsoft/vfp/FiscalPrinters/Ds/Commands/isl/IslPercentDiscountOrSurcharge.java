package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslPercentDiscountOrSurcharge implements Command {
    private final Boolean isDiscount;
    private final Integer amount;

    private IslPercentDiscountOrSurcharge(Boolean isDiscount, Integer amount) {
        this.isDiscount = isDiscount;
        this.amount = amount;
    }

    public static IslPercentDiscountOrSurcharge fromString(String string) {
        if(string.length() < 5) throw new RuntimeException("INVALID FORMAT");
        int amount = Integer.parseInt(string.substring(1));
        if(amount > 9999) throw new RuntimeException("PERCENT AMOUNT OUT OF BOUNDS");
        return new IslPercentDiscountOrSurcharge(
                Boolean.valueOf(string.substring(0,1)),
                amount
        );
    }

    public Boolean getDiscount() {
        return isDiscount;
    }

    public Integer getAmount() {
        return amount;
    }
}
