package eu.formenti.productpictures;

import jssc.SerialPortList;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

class UI extends JFrame {
    private JList products;
    private JLabel sku;
    private JList pictures;
    private JButton product360Button;
    private JButton productSingleButton;
    private JButton box360Button;
    private JButton boxSingleButton;
    private JButton openFolderButton;
    private JProgressBar progressBar;
    private JPanel panel;
    private JComboBox COMSelector;
    private JButton connectButton;

    private DefaultListModel<String> productsList;
    private Storage storage;

    UI(Storage storage) {
        this.storage = storage;
    }

    DefaultListModel<String> getProducts() {
        return productsList;
    }

    void loadProduct(String sku, DefaultListModel<String> images) {
        this.sku.setText(sku);
        this.pictures = new JList<>(images);
        pictures.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pictures.addListSelectionListener((event) -> {
            storage.openPicturesFolder((String) pictures.getSelectedValue(), this.sku.getText());
        });
    }

    JPanel getPanel() {
        return panel;
    }

    private void createUIComponents() {
        productsList = new DefaultListModel<>();
        products = new JList<>(productsList);
        products.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        products.addListSelectionListener((event) -> storage.loadSingle((String) products.getSelectedValue()));
        openFolderButton = new JButton("Open Folder");
        openFolderButton.addActionListener((event) -> storage.openFolder(this.sku.getText()));
        COMSelector = new JComboBox<String>();
        Arrays.stream(SerialPortList.getPortNames()).forEach((port) -> COMSelector.addItem(port));
        connectButton = new JButton("Connect");
        connectButton.addActionListener((event) -> storage.changeSerialConnector((String) COMSelector.getSelectedItem()));

        product360Button = new JButton("Product 360");
        product360Button.addActionListener((event) -> storage.createCapture(this.sku.getText(), "product", "360"));
        box360Button = new JButton("BOX 360");
        box360Button.addActionListener((event) -> storage.createCapture(this.sku.getText(), "box", "360"));
        productSingleButton = new JButton("Product Single");
        productSingleButton.addActionListener((event) -> storage.createCapture(this.sku.getText(), "product", "single"));
        boxSingleButton = new JButton("Box Single");
        boxSingleButton.addActionListener((event) -> storage.createCapture(this.sku.getText(), "box", "single"));
    }

    JProgressBar getProgressBar() {
        return progressBar;
    }

    void setPanelEnabled(JPanel panel, boolean isEnabled) {
        panel.setEnabled(isEnabled);
        Component[] components = panel.getComponents();

        for (Component component : components) {
            if (component instanceof JPanel)
                setPanelEnabled((JPanel) component, isEnabled);
            component.setEnabled(isEnabled);
        }
    }

    void disableSerial() {
        this.COMSelector.setEnabled(false);
        this.connectButton.setEnabled(false);
    }
}
