package com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.isl;

import com.viamindsoft.vfp.FiscalPrinters.Ds.Frames.Frame;

public interface IslFrame extends Frame {
    char STX = 0x02;
    char ETX = 0x03;
}
