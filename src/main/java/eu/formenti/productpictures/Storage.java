package eu.formenti.productpictures;

import jssc.SerialPort;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

class Storage {

    private UI ui;
    private File picturesFolder;
    private Camera camera;
    private SerialConnector serialConnector;

    Storage() {
        String picturesFolderName = "images";
        serialConnector = new SerialConnector();
        picturesFolder = new File(picturesFolderName);
        if (!picturesFolder.exists())
            picturesFolder.mkdir();
    }

    void setUi(UI ui) {
        this.ui = ui;
    }

    void load() {
        String filename = "products.csv";
        File sourceCsv = new File(filename);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sourceCsv));
            String line = "";
            while ((line = reader.readLine()) != null) {
                File productFolder = new File(picturesFolder.getAbsolutePath() + "\\" + line);
                if (productFolder.exists() && Objects.requireNonNull(productFolder.listFiles()).length > 0)
                    ui.getProducts().addElement(line + "✓");
                else
                    ui.getProducts().addElement(line);
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(ui.getPanel(), "Products CSV not found in " + sourceCsv.getAbsolutePath(), "Warning", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(ui.getPanel(), e.getLocalizedMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }

    void loadSingle(String sku) {
        File productFolder = new File(picturesFolder.getAbsolutePath() + "/" + sku);
        DefaultListModel<String> images = new DefaultListModel<>();
        if (productFolder.exists())
            for (File f : Objects.requireNonNull(productFolder.listFiles()))
                if (f.isDirectory() && f.getName().matches("\\d{1,8}-(box|product)-(360|single)-\\d{4}.\\d{1,2}.\\d{1,2}-\\d{1,2}.\\d{1,2}.\\d{1,2}")) {
                    String[] fields = f.getName().split("-");
                    images.addElement(fields[1] + " " + fields[2] + " " + fields[3].replace(".", "/"));
                }
        ui.loadProduct(sku, images);
    }

    void openFolder(String sku) {
        File folder = new File(picturesFolder.getAbsolutePath() + "/" + sku);
        if (folder.exists() && folder.isDirectory()) {
            try {
                Runtime.getRuntime().exec("explorer.exe /select," + folder.getAbsolutePath());
                return;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(ui.getPanel(), "Folder not found", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
        JOptionPane.showMessageDialog(ui.getPanel(), "Folder not found", "Warning", JOptionPane.WARNING_MESSAGE);
    }

    void openPicturesFolder(String name, String sku) {
        System.out.println(name);
        System.out.println(sku);
    }

    void changeSerialConnector(String COMPort) {
        serialConnector.changePort(new SerialPort(COMPort));
    }

    void setCamera(Camera camera) {
        this.camera = camera;
    }

    void createCapture(String sku, String operation, String type) {
        if (camera == null || serialConnector == null) {
            JOptionPane.showMessageDialog(ui.getPanel(), "Camera or serial connection non available", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (sku.equals("SKU")) {
            JOptionPane.showMessageDialog(ui.getPanel(), "No product selected", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        sku = sku.replace("✓", "");
        ui.setPanelEnabled(ui.getPanel(), false);
        SimpleDateFormat format = new SimpleDateFormat("yMdd-kmmss");
        File productFolder = new File(picturesFolder.getAbsolutePath() + "\\" + sku);
        if (!productFolder.exists())
            productFolder.mkdir();
        File setFolder = new File(String.format("%s\\%s-%s-%s-%s", productFolder.getAbsolutePath(), format.format(new Date()), sku, operation, type));
        setFolder.mkdir();
        ui.getProgressBar().setMinimum(0);
        ui.getProgressBar().setMaximum(20);
        String finalSku = sku;
        Thread t = new Thread(() -> {
            for (int i = 0; i < 20; i++) {
                try {
                    BufferedImage shoot = camera.takePicture();
                    ImageIO.write(shoot, "jpeg", new File(setFolder.getAbsoluteFile() + "\\" + i + ".jpg"));
                    serialConnector.advanceStep();
                    Thread.sleep(200);
                    int finalI = i;
                    SwingUtilities.invokeLater(() -> ui.getProgressBar().setValue(finalI));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(ui.getPanel(), e.getLocalizedMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
                    ui.setPanelEnabled(ui.getPanel(), true);
                    return;
                }
                loadSingle(finalSku);
            }
            SwingUtilities.invokeLater(() -> ui.getProgressBar().setValue(0));
            ui.setPanelEnabled(ui.getPanel(), true);
        });
        t.start();
    }
}
