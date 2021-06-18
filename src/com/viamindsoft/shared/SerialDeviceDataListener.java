package com.viamindsoft.shared;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.viamindsoft.vfp.FiscalDevice;


public class SerialDeviceDataListener implements SerialPortDataListener {
    private final FiscalDevice device;

    public SerialDeviceDataListener(FiscalDevice device) {
        this.device = device;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        device.handle(serialPortEvent);
    }
}
