package telas;

import grafo.Hospital;
import java.awt.*;
import javax.swing.*;

public class TelaCadastroHospital extends JDialog {

    private JTextField txtId;
    private JTextField txtNome;
    private JTextField txtLatitude;
    private JTextField txtLongitude;
    private JTextField txtCapacidade;
    private JTextField txtOcupacao;

    private JButton btnSalvar;
    private JButton btnCancelar;

    private Hospital hospitalCriado = null;

    public TelaCadastroHospital(JFrame parent) {
        super(parent, "Cadastro de Novo Hospital", true);
        setSize(400, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        criarComponentes();
    }

    private void criarComponentes() {
        setLayout(new BorderLayout());

        JPanel painelForm = new JPanel(new GridBagLayout());
        painelForm.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 4);

        // ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        painelForm.add(new JLabel("ID (Número):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtId = new JTextField();
        txtId.setPreferredSize(new Dimension(200, 26));
        painelForm.add(txtId, gbc);

        // Nome
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Nome do Hospital:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNome = new JTextField();
        txtNome.setPreferredSize(new Dimension(200, 26));
        painelForm.add(txtNome, gbc);

        // Latitude
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Latitude:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtLatitude = new JTextField();
        txtLatitude.setPreferredSize(new Dimension(200, 26));
        painelForm.add(txtLatitude, gbc);

        // Longitude
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Longitude:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtLongitude = new JTextField();
        txtLongitude.setPreferredSize(new Dimension(200, 26));
        painelForm.add(txtLongitude, gbc);

        // Capacidade
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Capacidade Máxima:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtCapacidade = new JTextField();
        txtCapacidade.setPreferredSize(new Dimension(200, 26));
        painelForm.add(txtCapacidade, gbc);

        // Ocupação
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Ocupação Atual:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtOcupacao = new JTextField("0");
        txtOcupacao.setPreferredSize(new Dimension(200, 26));
        painelForm.add(txtOcupacao, gbc);

        add(painelForm, BorderLayout.CENTER);

        // Painel inferior com os botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        btnSalvar = new JButton("Salvar Hospital");
        btnSalvar.setPreferredSize(new Dimension(140, 30));
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(110, 30));

        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    private void salvar() {
        try {
            if (txtNome.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "O nome do hospital é obrigatório!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = Integer.parseInt(txtId.getText().trim());
            String nome = txtNome.getText().trim();
            double latitude = Double.parseDouble(txtLatitude.getText().trim().replace(",", "."));
            double longitude = Double.parseDouble(txtLongitude.getText().trim().replace(",", "."));
            int capacidade = Integer.parseInt(txtCapacidade.getText().trim());
            int ocupacao = Integer.parseInt(txtOcupacao.getText().trim());

            if (capacidade <= 0) {
                JOptionPane.showMessageDialog(this, "A capacidade deve ser maior que zero!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            this.hospitalCriado = new Hospital(id, nome, latitude, longitude, capacidade, ocupacao);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Verifique os campos numéricos! ID, Capacidade e Ocupação devem ser números inteiros.\nLatitude e Longitude devem ser números reais.", 
                "Erro de Formatação", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Hospital getHospitalCriado() {
        return hospitalCriado;
    }
}