package com.viamindsoft.vfp.FiscalPrinters.Ds;

import java.util.Map;

public class FiscalPrinterSerialImpl implements FiscalPrinterSerial {

    private static final byte STX = 0x02;
    private static final byte ETX = 0x03;
    private static final Map<String,Integer> manufacturer = Map.of(
            "IS", 1,
            "DY",1,
            "DT",1
    );
    private final String serialNumber;

    private FiscalPrinterSerialImpl(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public static FiscalPrinterSerial factory(byte[] bytes) {
        int start = 0; int length = 8;
        if(bytes[0] == STX) start+=1;
        StringBuilder sb = new StringBuilder();
        for(var i = start; i < length ; i++) {
            sb.append(bytes[i]);
        }
        return factory(sb.toString());
    }

    public static FiscalPrinterSerial factory(String serialString) {
        verifyStringFormat(serialString);
        return new FiscalPrinterSerialImpl(serialString);
    }

    private static void verifyStringFormat(String serialNumber) {
        if(!manufacturer.containsKey(serialNumber.substring(0,2)))
            throw new RuntimeException("INVALID SERIAL");
        String stripManufacturer = serialNumber.substring(2);
        for(var i = 0; i < stripManufacturer.length(); i++) {
            try {
                Integer.parseInt(String.valueOf(stripManufacturer.charAt(i)));
            } catch (NumberFormatException exception) {
                throw new RuntimeException("INVALID SERIAL IN NUMS");
            }
        }


    }

    @Override
    public String toString() {
        return serialNumber;
    }

    @Override
    public String networkString() {
        return serialNumber.substring(serialNumber.length() - 4);
    }
}
