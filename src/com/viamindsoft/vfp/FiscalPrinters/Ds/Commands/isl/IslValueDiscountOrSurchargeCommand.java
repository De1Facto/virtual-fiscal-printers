package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslValueDiscountOrSurchargeCommand implements Command {
    private final Boolean isDiscount;
    private final Long amount;
    private final ISLFiscalPrinter printer;

    private IslValueDiscountOrSurchargeCommand(Boolean isDiscount, Long amount,ISLFiscalPrinter printer) {
        this.isDiscount = isDiscount;
        this.amount = amount;
        this.printer = printer;
    }

    public static IslValueDiscountOrSurchargeCommand fromString(String inputString,ISLFiscalPrinter printer) {
        long amount = Long.parseLong(inputString.substring(1));
        if(amount > 99999999) throw new RuntimeException("PRICE OUT OF BOUNDS");
        return new IslValueDiscountOrSurchargeCommand(
                Boolean.valueOf(inputString.substring(0,1)),
                amount,
                printer
        );
    }

    public Boolean getDiscount() {
        return isDiscount;
    }

    public Long getAmount() {
        return amount;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
