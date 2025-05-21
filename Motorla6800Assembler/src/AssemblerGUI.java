import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AssemblerGUI extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextArea inputArea;
    private JTextArea outputArea;
    private JButton compileButton;

    public AssemblerGUI() {
        setTitle("Motorola 6800 Assembler");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Sol taraf: Assembly kod girişi
        inputArea = new JTextArea();
        inputArea.setBorder(BorderFactory.createTitledBorder("Assembly Kodu"));
        JScrollPane inputScroll = new JScrollPane(inputArea);

        // Sağ taraf: Nesne kodu çıkışı
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setBorder(BorderFactory.createTitledBorder("Derleme Sonucu"));
        JScrollPane outputScroll = new JScrollPane(outputArea);

        // Ortadaki buton
        compileButton = new JButton("Derle");
        compileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                compileAssembly();
            }
        });

        // Panellerin yerleşimi
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.add(inputScroll);
        centerPanel.add(outputScroll);

        add(centerPanel, BorderLayout.CENTER);
        add(compileButton, BorderLayout.SOUTH);
    }

    private void compileAssembly() {
        String input = inputArea.getText();
        String output = Assembler6800.assemble(input);  // Assembler6800.java sınıfına çağrı
        outputArea.setText(output);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new AssemblerGUI().setVisible(true);
        });
    }
}
