package eu.formenti.productpictures;

import javax.swing.*;
class Main {
    static UI ui;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            Camera camera = new Camera();
            Storage storage = new Storage();
            ui = new UI(storage);
            storage.setUi(ui);
            storage.setCamera(camera);
            storage.load();

            JFrame frame = new JFrame("Product Pictures");
            frame.setContentPane(ui.getPanel());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new JFrame(), e.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }
}