package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.ISLFiscalPrinter;

public class IslItemReversalCommand extends IslItemSaleCommand
{
    protected IslItemReversalCommand(String uniqueSaleNumber,
                                     long quantity,
                                     long number,
                                     long price,
                                     int department,
                                     int taxGroup,
                                     int singleTransaction,
                                     boolean flag,
                                     String name,
                                     ISLFiscalPrinter printer) {
        super(uniqueSaleNumber, quantity, number, price, department, taxGroup, singleTransaction, flag, name,printer);
    }

    public static IslItemReversalCommand fromString(String inputString, ISLFiscalPrinter printer) {
        return new IslItemReversalCommand(
                inputString.substring(0,21),
                parseLong(inputString.substring(21),8,99999999L),
                parseLong(inputString.substring(29),8,99999999L),
                parseLong(inputString.substring(37),8,1000000L),
                parseDepartment(inputString.charAt(45)),
                parseTaxGroup(inputString.charAt(46)),
                parseTransaction(inputString.charAt(47)),
                parseFlag(inputString.charAt(48)),
                inputString.substring(49),
                printer
        );
    }
}
