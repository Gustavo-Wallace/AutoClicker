import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoClickerGUI implements NativeKeyListener {
    private final AtomicBoolean ativo = new AtomicBoolean(false);
    private Thread threadExecucao;
    private int intervalo = 100;
    private boolean modoMouse = true;
    private int botaoMouse = InputEvent.BUTTON1_DOWN_MASK;
    private int teclaPressionar = KeyEvent.VK_SPACE;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AutoClickerGUI::new);
    }

    public AutoClickerGUI() {
        JFrame frame = new JFrame("AutoClicker | F6 para iniciar");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);

        JLabel lblIntervalo = new JLabel("Intervalo (ms):");
        JTextField txtIntervalo = new JTextField("100", 5);

        JLabel lblModo = new JLabel("Modo:");
        JComboBox<String> cbModo = new JComboBox<>(new String[]{"Mouse", "Teclado"});

        JLabel lblBotao = new JLabel("Botão:");
        JComboBox<String> cbBotao = new JComboBox<>(new String[]{"Esquerdo", "Direito", "Meio"});

        JLabel lblTecla = new JLabel("Tecla:");
        JTextField txtTecla = new JTextField("SPACE", 10);

        JButton btnToggle = new JButton("Aplicar");

        JPanel painel = new JPanel();
        painel.setLayout(new GridLayout(6, 2, 10, 5));
        painel.add(lblIntervalo);
        painel.add(txtIntervalo);
        painel.add(lblModo);
        painel.add(cbModo);
        painel.add(lblBotao);
        painel.add(cbBotao);
        painel.add(lblTecla);
        painel.add(txtTecla);
        painel.add(new JLabel());
        painel.add(btnToggle);

        cbModo.addActionListener(e -> {
            boolean ehMouse = cbModo.getSelectedItem().equals("Mouse");
            lblBotao.setVisible(ehMouse);
            cbBotao.setVisible(ehMouse);
            lblTecla.setVisible(!ehMouse);
            txtTecla.setVisible(!ehMouse);
        });
        cbModo.setSelectedIndex(0);

        btnToggle.addActionListener(e -> {
            if (!ativo.get()) {
                try {
                    intervalo = Integer.parseInt(txtIntervalo.getText());
                    modoMouse = cbModo.getSelectedItem().equals("Mouse");

                    if (modoMouse) {
                        botaoMouse = switch (cbBotao.getSelectedItem().toString()) {
                            case "Esquerdo" -> InputEvent.BUTTON1_DOWN_MASK;
                            case "Direito"  -> InputEvent.BUTTON3_DOWN_MASK;
                            case "Meio"     -> InputEvent.BUTTON2_DOWN_MASK;
                            default -> InputEvent.BUTTON1_DOWN_MASK;
                        };
                    } else {
                        String teclaStr = txtTecla.getText().trim().toUpperCase();
                        switch (teclaStr) {
                            case "ENTER" -> teclaPressionar = KeyEvent.VK_ENTER;
                            case "SPACE", "ESPACO" -> teclaPressionar = KeyEvent.VK_SPACE;
                            case "ESC", "ESCAPE" -> teclaPressionar = KeyEvent.VK_ESCAPE;
                            case "TAB" -> teclaPressionar = KeyEvent.VK_TAB;
                            case "SHIFT" -> teclaPressionar = KeyEvent.VK_SHIFT;
                            case "CTRL", "CONTROL" -> teclaPressionar = KeyEvent.VK_CONTROL;
                            case "ALT" -> teclaPressionar = KeyEvent.VK_ALT;
                            case "BACKSPACE" -> teclaPressionar = KeyEvent.VK_BACK_SPACE;
                            case "DELETE", "DEL" -> teclaPressionar = KeyEvent.VK_DELETE;
                            case "UP" -> teclaPressionar = KeyEvent.VK_UP;
                            case "DOWN" -> teclaPressionar = KeyEvent.VK_DOWN;
                            case "LEFT" -> teclaPressionar = KeyEvent.VK_LEFT;
                            case "RIGHT" -> teclaPressionar = KeyEvent.VK_RIGHT;
                            default -> {
                                if (teclaStr.length() == 1) {
                                    char c = teclaStr.charAt(0);
                                    teclaPressionar = KeyEvent.getExtendedKeyCodeForChar(c);
                                } else {
                                    throw new IllegalArgumentException("Tecla inválida");
                                }
                            }
                        }
                    }

                    iniciar();
                    btnToggle.setText("Parar");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Erro: valores inválidos");
                }
            } else {
                parar();
                btnToggle.setText("Iniciar");
            }
        });

        frame.add(painel);
        frame.setVisible(true);

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void iniciar() {
        ativo.set(true);
        threadExecucao = new Thread(() -> {
            try {
                Robot robot = new Robot();
                while (ativo.get()) {
                    if (modoMouse) {
                        robot.mousePress(botaoMouse);
                        robot.mouseRelease(botaoMouse);
                    } else {
                        robot.keyPress(teclaPressionar);
                        robot.keyRelease(teclaPressionar);
                    }
                    Thread.sleep(intervalo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        threadExecucao.start();
    }

    private void parar() {
        ativo.set(false);
        if (threadExecucao != null) {
            threadExecucao.interrupt();
        }
    }

    @Override public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F6) {
            if (ativo.get()) {
                parar();
                System.out.println("AutoClicker parado (F6)");
            } else {
                iniciar();
                System.out.println("AutoClicker iniciado (F6)");
            }
        }
    }

    @Override public void nativeKeyReleased(NativeKeyEvent e) {}
    @Override public void nativeKeyTyped(NativeKeyEvent e) {}
}
