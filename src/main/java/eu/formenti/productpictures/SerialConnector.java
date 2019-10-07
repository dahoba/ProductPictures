package eu.formenti.productpictures;

import jssc.SerialPort;
import jssc.SerialPortException;

import javax.swing.*;

class SerialConnector {
    private SerialPort serialPort;


    void changePort(SerialPort port) {
        try {
            if (this.serialPort != null)
                this.serialPort.closePort();
            serialPort = port;
            port.openPort();
            Main.ui.disableSerial();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(Main.ui.getPanel(), e.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    void advanceStep() throws SerialPortException {
        if (serialPort.isOpened()) {
            serialPort.writeByte((byte)1);
        }
    }

}
