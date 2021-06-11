package com.viamindsoft.vfp;


import com.viamindsoft.vfp.FiscalPrinters.Ds.Commands.Command;
import com.viamindsoft.vfp.FiscalPrinters.Ds.Responses.Response;

public interface FiscalPrinter  {
    FiscalPrinterData fiscalPrinterData();
    void execute(Command command);
    Response getResponse();
}
