package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslVoidCommand implements Command {
    private final Boolean voidJustLast;
    private final Long quantity;

    private IslVoidCommand(Boolean voidJustLast, Long quantity) {
        this.voidJustLast = voidJustLast;
        this.quantity = quantity;
    }

    public static IslVoidCommand fromString(String inputString) {
        long quantity = Long.MAX_VALUE;
        if(inputString.length() > 1) {
            quantity = Long.parseLong(inputString.substring(1));
        }
        return new IslVoidCommand(
                Boolean.valueOf(inputString.substring(0,1)),
                quantity
        );
    }

    public Boolean getVoidJustLast() {
        return voidJustLast;
    }

    public Long getQuantity() {
        return quantity;
    }
}
