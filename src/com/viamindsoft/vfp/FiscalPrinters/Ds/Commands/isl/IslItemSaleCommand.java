package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;

public class IslItemSaleCommand implements Command {

    protected String uniqueSaleNumber;
    protected long quantity;
    protected long number;
    protected long price;
    protected byte department;
    protected byte taxGroup;
    protected byte singleTransaction;
    protected boolean flag;
    protected String name;


    protected IslItemSaleCommand(String uniqueSaleNumber, long quantity, long number, long price, byte department, byte taxGroup, byte singleTransaction, boolean flag, String name) {
        this.uniqueSaleNumber = uniqueSaleNumber;
        this.quantity = quantity;
        this.number = number;
        this.price = price;
        this.department = department;
        this.taxGroup = taxGroup;
        this.singleTransaction = singleTransaction;
        this.flag = flag;
        this.name = name;
    }

    public static IslItemSaleCommand fromString(String inputString) {
        return new IslItemSaleCommand(
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

    protected static long parseLong(String string,int length, long maxValue) {
        if(string.length() < length) return 0L;
        long value = Long.parseLong(string.substring(0,length));
        if(value > maxValue) throw new RuntimeException("INVALID VALUE FOR QUANT/PRICE or NUM");
        return value;
    }

    protected static byte parseDepartment(char c) {
        byte result = (byte) c;
        if(result > 10 || result < 0) throw new RuntimeException("INVALID DEPARTMENT");
        return result;
    }

    protected static byte parseTaxGroup(char c) {
        byte result = (byte) c;
        if(result > 8 || result < 1) throw new RuntimeException("INVALID TAX GROUP");
        return result;
    }
    protected static byte parseTransaction(char c) {
        byte result = (byte) c;
        if(result > 3 || result < 0) throw new RuntimeException("INVALID TAX TRANSACTION TYPE");
        return result;
    }

    protected static boolean parseFlag(char c) {
        byte result = (byte) c;
        return result > 0;
    }

    public String getUniqueSaleNumber() {
        return uniqueSaleNumber;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getNumber() {
        return number;
    }

    public long getPrice() {
        return price;
    }

    public byte getDepartment() {
        return department;
    }

    public byte getTaxGroup() {
        return taxGroup;
    }

    public byte getSingleTransaction() {
        return singleTransaction;
    }

    public boolean isFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }
}
