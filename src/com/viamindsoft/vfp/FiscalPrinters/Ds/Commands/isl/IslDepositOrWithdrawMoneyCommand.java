package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslDepositOrWithdrawMoneyCommand implements Command {
    private final Boolean isWithdraw;
    private final Short paymentType;
    private final Long amount;
    private final ISLFiscalPrinter printer;

    public IslDepositOrWithdrawMoneyCommand(Boolean isWithdraw, Short paymentType, Long amount, ISLFiscalPrinter printer) {
        this.isWithdraw = isWithdraw;
        this.paymentType = paymentType;
        this.amount = amount;
        this.printer = printer;
    }

    public static IslDepositOrWithdrawMoneyCommand fromString(String s,ISLFiscalPrinter printer) {
        if(s.length() < 15) throw new RuntimeException("INVALID FORMAT FOR DEPOSIT/WITHDRAW");
        short paymentType = Short.parseShort(s.substring(1, 3));
        if(paymentType > 10 || paymentType < 0) throw new RuntimeException("INVALID PAYMENT TYPE");
        long amount = Long.parseLong(s.substring(3));
        if(amount < 1 || amount > 999999999L) throw new RuntimeException("INVALID PAYMENT AMOUNT");
        return new IslDepositOrWithdrawMoneyCommand(
                Boolean.parseBoolean(s.substring(0,1)),
                paymentType,
                amount,
                printer);
    }

    public Boolean getWithdraw() {
        return isWithdraw;
    }

    public Short getPaymentType() {
        return paymentType;
    }

    public Long getAmount() {
        return amount;
    }

    @Override
    public void execute() {
        printer.execute(this);
    }
}
