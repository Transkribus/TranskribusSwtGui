package examples;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SystemFontDisplayer extends JFrame {
  DisplayPanel displayPanel;

  String[] fontStyleLabels = { "Plain", "Bold", "Italic", "Bold&Italic" };
  int BOLDITALIC = Font.BOLD | Font.ITALIC;
  int[] fontStyles = { Font.PLAIN, Font.BOLD, Font.ITALIC, BOLDITALIC };
  String[] fontSizeLabels = { "8", "9", "10", "11", "12", "14", "18", "25",
      "36", "72" };
  JComboBox fontsBox, 
  fontStylesBox = new JComboBox(fontStyleLabels), fontSizesBox  = new JComboBox(fontSizeLabels);

  public SystemFontDisplayer() {
    Container container = getContentPane();
    displayPanel = new DisplayPanel();
    container.add(displayPanel);
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new GridLayout(1, 3));

    fontsBox= new JComboBox(displayPanel.fontFamilyNames);
    fontsBox.setSelectedItem("Arial"); 
    fontsBox.addActionListener(new ComboBoxListener());

    fontStylesBox.addActionListener(new ComboBoxListener());

    fontSizesBox.setSelectedItem("36");
    fontSizesBox.addActionListener(new ComboBoxListener());
    controlPanel.add(fontsBox);
    controlPanel.add(fontStylesBox);
    controlPanel.add(fontSizesBox);
    container.add(BorderLayout.SOUTH, controlPanel);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    pack();
    setSize(400, 250);
    setVisible(true);
  }

  public static void main(String arg[]) {
    new SystemFontDisplayer();
  }

  class ComboBoxListener implements ActionListener {
    @Override
	public void actionPerformed(ActionEvent e) {
      JComboBox tempBox = (JComboBox) e.getSource();

      if (tempBox.equals(fontsBox)) {
        displayPanel.fontFamilyName = (String) tempBox.getSelectedItem();
        displayPanel.repaint();
      } else if (tempBox.equals(fontStylesBox)) {
        displayPanel.fontStyle = fontStyles[tempBox.getSelectedIndex()];
        displayPanel.repaint();
      } else if (tempBox.equals(fontSizesBox)) {
        displayPanel.fontSize = Integer.parseInt((String) tempBox
            .getSelectedItem());
        displayPanel.repaint();
      }
    }
  }

  class DisplayPanel extends JPanel {
    String fontFamilyName;
    int fontStyle;
    int fontSize;
    String[] fontFamilyNames;
    public DisplayPanel() {
      fontFamilyName = "Arial";
      fontStyle = Font.PLAIN;
      fontSize = 36;
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      fontFamilyNames = ge.getAvailableFontFamilyNames();
      setSize(400, 225); 
    }
    @Override
	public void update(Graphics g) {
      g.clearRect(0, 0, getWidth(), getHeight());
      paintComponent(g);
    }
    @Override
	public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2D = (Graphics2D) g;
      g2D.setFont(new Font(fontFamilyName, fontStyle, fontSize));
      g2D.drawString("Java 2D Fonts", 25, 100);
    }
  }
}