package com.viamindsoft.vfp.FiscalPrinters.Ds.Commands;

public interface CommandFactory {
    Command createCommand(String inputString);
    Command createCommand(byte[] inputBytes);
}
