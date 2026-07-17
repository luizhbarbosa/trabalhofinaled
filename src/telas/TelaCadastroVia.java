package telas;

import grafo.Aresta;
import grafo.GrafoCidade;
import grafo.StatusVia;
import grafo.Vertice;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TelaCadastroVia extends JDialog {

    private JComboBox<Vertice> cbOrigem;
    private JComboBox<Vertice> cbDestino;
    private JTextField txtNome;
    private JTextField txtDistancia;
    private JComboBox<StatusVia> cbStatus;
    private JCheckBox chkBidirecional;

    private JButton btnSalvar;
    private JButton btnCancelar;
    private JButton btnCalcularAuto;

    private List<Aresta> arestasCriadas = new ArrayList<>();
    private GrafoCidade grafo;

    public TelaCadastroVia(JFrame parent, GrafoCidade grafo) {
        super(parent, "Cadastro de Nova Via (Rua)", true);
        this.grafo = grafo;

        setSize(450, 340);
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

        // Renderer para mostrar apenas o nome do vértice (sem "Vertice{id=...}")
        java.util.function.Function<Vertice, String> formatador = v -> v != null ? v.getNome() + " (" + v.getTipo().getDescricao() + ")" : "";

        // 1. Ponto de Origem
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Ponto de Origem:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbOrigem = new JComboBox<>(carregarVertices());
        cbOrigem.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, 
                    value instanceof Vertice ? formatador.apply((Vertice) value) : value, 
                    index, isSelected, cellHasFocus);
                return this;
            }
        });
        cbOrigem.setPreferredSize(new Dimension(220, 26));
        cbOrigem.addActionListener(e -> calcularDistanciaAutomatica());
        painelForm.add(cbOrigem, gbc);

        // 2. Ponto de Destino
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Ponto de Destino:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbDestino = new JComboBox<>(carregarVertices());
        cbDestino.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, 
                    value instanceof Vertice ? formatador.apply((Vertice) value) : value, 
                    index, isSelected, cellHasFocus);
                return this;
            }
        });
        cbDestino.setPreferredSize(new Dimension(220, 26));
        cbDestino.addActionListener(e -> calcularDistanciaAutomatica());
        painelForm.add(cbDestino, gbc);

        // 3. Nome da Rua
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Nome da Rua/Av.:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtNome = new JTextField();
        txtNome.setPreferredSize(new Dimension(220, 26));
        painelForm.add(txtNome, gbc);

        // 4. Distância
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Distância (km):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        JPanel painelDistancia = new JPanel(new BorderLayout(5, 0));
        txtDistancia = new JTextField();
        txtDistancia.setPreferredSize(new Dimension(170, 26));
        btnCalcularAuto = new JButton("⚡ Auto");
        btnCalcularAuto.setToolTipText("Calcular distância pela linha reta entre os pontos");
        btnCalcularAuto.addActionListener(e -> calcularDistanciaAutomatica());
        btnCalcularAuto.setPreferredSize(new Dimension(70, 26));
        
        painelDistancia.add(txtDistancia, BorderLayout.CENTER);
        painelDistancia.add(btnCalcularAuto, BorderLayout.EAST);
        painelForm.add(painelDistancia, gbc);

        // 5. Status inicial da via
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Status Tráfego:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbStatus = new JComboBox<>(StatusVia.values());
        cbStatus.setPreferredSize(new Dimension(220, 26));
        painelForm.add(cbStatus, gbc);

        // 6. Via Bidirecional
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Sentido Duplo:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        chkBidirecional = new JCheckBox("Criar Ida e Volta", true);
        chkBidirecional.setFont(new Font("SansSerif", Font.PLAIN, 12));
        painelForm.add(chkBidirecional, gbc);

        add(painelForm, BorderLayout.CENTER);

        // Faz o primeiro cálculo ao abrir a tela
        calcularDistanciaAutomatica();

        // Painel inferior de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        btnSalvar = new JButton("Salvar Via");
        btnSalvar.setPreferredSize(new Dimension(130, 30));
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(110, 30));

        btnSalvar.addActionListener(e -> salvar());
        btnCancelar.addActionListener(e -> dispose());

        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    private Vertice[] carregarVertices() {
        if (grafo == null || grafo.getVertices().isEmpty()) {
            return new Vertice[0];
        }
        return grafo.getVertices().toArray(new Vertice[0]);
    }

    private void calcularDistanciaAutomatica() {
        Vertice origem = (Vertice) cbOrigem.getSelectedItem();
        Vertice destino = (Vertice) cbDestino.getSelectedItem();

        if (origem != null && destino != null) {
            if (origem.getId() == destino.getId()) {
                txtDistancia.setText("0.0");
            } else {
                double dist = origem.calcularDistancia(destino);
                dist = Math.max(0.1, dist);
                txtDistancia.setText(String.format("%.1f", dist).replace(",", "."));
            }
        }
    }

    private void salvar() {
        try {
            Vertice origem = (Vertice) cbOrigem.getSelectedItem();
            Vertice destino = (Vertice) cbDestino.getSelectedItem();

            if (origem == null || destino == null) {
                JOptionPane.showMessageDialog(this, "Selecione a origem e o destino!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (origem.getId() == destino.getId()) {
                JOptionPane.showMessageDialog(this, "A origem e o destino não podem ser o mesmo ponto!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String nome = txtNome.getText().trim();
            if (nome.isEmpty()) {
                nome = "Via " + origem.getNome() + " - " + destino.getNome();
            }

            double distanciaKm = Double.parseDouble(txtDistancia.getText().trim().replace(",", "."));
            if (distanciaKm <= 0) {
                JOptionPane.showMessageDialog(this, "A distância deve ser maior que zero!", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double tempoEmMinutos = (distanciaKm / 40.0) * 60.0;
            StatusVia status = (StatusVia) cbStatus.getSelectedItem();

            Aresta viaIda = new Aresta(origem, destino, tempoEmMinutos, status, nome);
            arestasCriadas.add(viaIda);

            if (chkBidirecional.isSelected()) {
                Aresta viaVolta = new Aresta(destino, origem, tempoEmMinutos, status, nome);
                arestasCriadas.add(viaVolta);
            }

            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite um número válido para a distância (ex: 2.5).", "Erro de Formatação", JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<Aresta> getArestasCriadas() {
        return arestasCriadas;
    }
}