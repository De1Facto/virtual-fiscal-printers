package com.viamindsoft.vfp;

import com.viamindsoft.vfp.FiscalPrinters.Ds.supportingDs.isl.IslBitwiseStatus;
import com.viamindsoft.vfp.FiscalPrinters.Ds.supportingDs.isl.IslBitwiseStatusImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FiscalPrinterDataImpl implements IslFiscalPrinterData {
    private final String serialNumber;
    private final String fiscalMemoryNum;
    private final String fiscalPrinterModel;
    private long lastReceipt = 0;
    private final long lastInvoiceNum = 0;
    private long invoiceStart = 1;
    private long invoiceEnd = 999_999_999;
    private int errorCode = 0;
    private IslBitwiseStatus bitwiseStatus = IslBitwiseStatusImpl.fromStatusArray(
            new byte[][]{
                    new byte[]{ 0x00, 0x01 },
                    new byte[]{ 0x00, 0x08 },
                    new byte[]{ 0x02, 0x07 },
                    new byte[]{ 0x00, 0x00 },
                    new byte[]{ 0x00, 0x00 },
                    new byte[]{ 0x00, 0x00 },
            }
    );

    private Map<Integer,Long> paymentTotals = new HashMap<>();
    private Map<Integer,Integer> paymentsMappings = new HashMap<>();


    private FiscalPrinterDataImpl(String serialNumber, String fiscalMemoryNum, String fiscalPrinterModel, long invoiceStart, long invoiceEnd) {
        this.serialNumber = serialNumber;
        this.fiscalMemoryNum = fiscalMemoryNum;
        this.invoiceStart = invoiceStart;
        this.invoiceEnd = invoiceEnd;
        this.fiscalPrinterModel = fiscalPrinterModel;
        initializePaymentTotals();
        initializePaymentsMappings();

    }

    private void initializePaymentTotals() {
        for(var i =0; i < 10; i++) {
            paymentTotals.put(i,0L);
        }
    }
    private void initializePaymentsMappings() {
        for(var i = 0; i < 11; i++) {
            paymentsMappings.put(i,i);
        }
    }

    public static FiscalPrinterDataImpl factory(String serialNumber, String fiscalMemoryNum, String fiscalPrinterModel, long invoiceStart, long invoiceEnd) {
        return new FiscalPrinterDataImpl(serialNumber,fiscalMemoryNum,fiscalPrinterModel,invoiceStart,invoiceEnd);
    }

    public static FiscalPrinterDataImpl factory(String serialNumber, String fiscalMemoryNum,String fiscalPrinterModel) {
        return new FiscalPrinterDataImpl(serialNumber,fiscalMemoryNum,fiscalPrinterModel,1L,999_999_999);
    }

    @Override
    public String serialNumber() {
        return serialNumber;
    }

    @Override
    public String fiscalMemoryNum() {
        return fiscalMemoryNum;
    }

    @Override
    public String fiscalPrinterModel() {
        return fiscalPrinterModel;
    }

    @Override
    public long lastReceipt() {
        return lastReceipt;
    }

    @Override
    public long lastInvoiceNum() {
        return lastInvoiceNum;
    }

    @Override
    public long invoiceStart() {
        return invoiceStart;
    }

    @Override
    public long invoiceEnd() {
        return invoiceEnd;
    }

    @Override
    public String errorCode() {
        return fillIntUpToCharsWithZeroesUpfront(errorCode,3);
    }

    private @NotNull
    String fillIntUpToCharsWithZeroesUpfront(Integer target, int length) {
        String targetString = target.toString();
        return "0".repeat(Math.max(0, (length - targetString.length()))) +
                targetString;
    }

    @Override
    public boolean hasError() {
        return errorCode > 0;
    }

    @Override
    public void setError(String error) {
        errorCode = Integer.parseInt(error);
    }

    @Override
    public void incrementInvoice() {
        invoiceStart++;
    }

    @Override
    public void incrementLastReceipt() {
        lastReceipt++;
    }

    @Override
    public void setInvoiceRange(long start, long end) {
        invoiceStart = start; invoiceEnd = end;
    }

    @Override
    public Map<Integer, Long> paymentsTotals() {
        return paymentTotals;
    }

    @Override
    public void clearTotals() {
        paymentTotals.values().forEach(total -> total = 0L);
    }

    @Override
    public void updateTotals(Map<Integer, Long> payments) {
        for(Map.Entry<Integer,Long> entry : payments.entrySet()) {
            addTotal(entry.getKey(),entry.getValue());
        }
    }


    public void addTotal(int paymentType, Long amount) {
        System.out.println("PaymentType: "+paymentType+" amount to add: "+ amount);
        paymentTotals.put(paymentType,paymentTotals.get(paymentType) + amount);
    }

    @Override
    public boolean attemptToClearErrors() {
        if(errorCode < 200) {
            errorCode = 0;
            return true;
        }
        return false;
    }

    public IslBitwiseStatus getBitwiseStatus() {
        return bitwiseStatus;
    }

    public Map<Integer, Integer> getPaymentsMappings() {
        return paymentsMappings;
    }
}
