package com.viamindsoft.vfp;

import com.viamindsoft.vfp.FiscalPrinters.Ds.supportingDs.isl.IslBitwiseStatus;

public interface IslFiscalPrinterData extends FiscalPrinterData {
    IslBitwiseStatus getBitwiseStatus();
}
