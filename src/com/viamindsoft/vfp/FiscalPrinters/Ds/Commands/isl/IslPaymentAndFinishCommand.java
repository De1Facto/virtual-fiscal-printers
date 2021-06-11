package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslPaymentAndFinishCommand implements Command {
    private final Short payment;
    private final Long amountGiven;
    private final Boolean flag;

    private IslPaymentAndFinishCommand(Short payment, Long amountGiven, Boolean flag) {
        this.payment = payment;
        this.amountGiven = amountGiven;
        this.flag = flag;
    }

    public static IslPaymentAndFinishCommand fromString(String inputString) {
        if(inputString.length() == 1)
            return defaultFinish(inputString);
        if(inputString.length() < 13) throw new RuntimeException("INVALID FORMAT");
        return new IslPaymentAndFinishCommand(
                Short.parseShort(inputString.substring(0,1)),
                Long.parseLong(inputString.substring(1,12)),
                Boolean.parseBoolean(inputString.substring(12,13))
        );
    }
    private static IslPaymentAndFinishCommand defaultFinish(String string) {
        return new IslPaymentAndFinishCommand(Short.parseShort(string),0L,false);
    }

    public Short getPayment() {
        return payment;
    }

    public Long getAmountGiven() {
        return amountGiven;
    }

    public Boolean getFlag() {
        return flag;
    }
}
