package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

public class IslItemReversalCommand extends IslItemSaleCommand
{
    protected IslItemReversalCommand(String uniqueSaleNumber, long quantity, long number, long price, byte department, byte taxGroup, byte singleTransaction, boolean flag, String name) {
        super(uniqueSaleNumber, quantity, number, price, department, taxGroup, singleTransaction, flag, name);
    }

    public static IslItemReversalCommand fromString(String inputString) {
        return new IslItemReversalCommand(
                inputString.substring(0,21),
                parseLong(inputString.substring(21),8,99999999L),
                parseLong(inputString.substring(29),8,99999999L),
                parseLong(inputString.substring(37),8,1000000L),
                parseDepartment(inputString.charAt(45)),
                parseTaxGroup(inputString.charAt(46)),
                parseTransaction(inputString.charAt(47)),
                parseFlag(inputString.charAt(48)),
                inputString.substring(49)
        );
    }
}
