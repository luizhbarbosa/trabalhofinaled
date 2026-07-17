package grafo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * TelaCadastroHospital — RF01 / camada de apresentação (RNF02).
 *
 * Funções (conforme especificação):
 *  - Cadastrar novos hospitais
 *  - Editar informações dos hospitais
 *  - Consultar capacidade de atendimento
 *  - Remover hospitais da rede
 */
public class TelaCadastroHospital extends JFrame {

    private final DefaultTableModel modeloTabela;
    private final JTable tabelaHospitais;

    private final JTextField txtId;
    private final JTextField txtNome;
    private final JTextField txtLatitude;
    private final JTextField txtLongitude;
    private final JTextField txtCapacidadeMaxima;
    private final JTextField txtOcupacaoAtual;

    private final JButton btnNovo;
    private final JButton btnSalvar;
    private final JButton btnRemover;
    private final JButton btnLimpar;

    private Hospital hospitalEmEdicao; // null = modo "novo cadastro"

    public TelaCadastroHospital() {
        super("SAMU — Cadastro de Hospitais");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(780, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ---------- Tabela (lista de hospitais) ----------
        String[] colunas = {"ID", "Nome", "Latitude", "Longitude", "Capacidade Máx.", "Ocupação", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // edição só pelo formulário lateral
            }
        };
        tabelaHospitais = new JTable(modeloTabela);
        tabelaHospitais.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                carregarSelecaoNoFormulario();
            }
        });
        JScrollPane scrollTabela = new JScrollPane(tabelaHospitais);
        scrollTabela.setBorder(BorderFactory.createTitledBorder("Hospitais cadastrados"));

        // ---------- Formulário ----------
        JPanel painelFormulario = new JPanel(new GridBagLayout());
        painelFormulario.setBorder(BorderFactory.createTitledBorder("Dados do hospital"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        txtId = new JTextField();
        txtId.setEditable(false);
        txtNome = new JTextField();
        txtLatitude = new JTextField();
        txtLongitude = new JTextField();
        txtCapacidadeMaxima = new JTextField();
        txtOcupacaoAtual = new JTextField();

        int linha = 0;
        adicionarCampo(painelFormulario, c, linha++, "ID (gerado):", txtId);
        adicionarCampo(painelFormulario, c, linha++, "Nome:", txtNome);
        adicionarCampo(painelFormulario, c, linha++, "Latitude:", txtLatitude);
        adicionarCampo(painelFormulario, c, linha++, "Longitude:", txtLongitude);
        adicionarCampo(painelFormulario, c, linha++, "Capacidade máxima:", txtCapacidadeMaxima);
        adicionarCampo(painelFormulario, c, linha++, "Ocupação atual:", txtOcupacaoAtual);

        // ---------- Botões ----------
        btnNovo = new JButton("Novo");
        btnSalvar = new JButton("Salvar");
        btnRemover = new JButton("Remover");
        btnLimpar = new JButton("Limpar");

        btnNovo.addActionListener(this::aoClicarNovo);
        btnSalvar.addActionListener(this::aoClicarSalvar);
        btnRemover.addActionListener(this::aoClicarRemover);
        btnLimpar.addActionListener(e -> limparFormulario());

        JPanel painelBotoes = new JPanel(new GridLayout(1, 4, 8, 0));
        painelBotoes.add(btnNovo);
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnLimpar);

        c.gridx = 0;
        c.gridy = linha;
        c.gridwidth = 2;
        painelFormulario.add(painelBotoes, c);

        JPanel painelDireito = new JPanel(new BorderLayout());
        painelDireito.add(painelFormulario, BorderLayout.NORTH);
        painelDireito.setPreferredSize(new Dimension(300, 0));

        add(scrollTabela, BorderLayout.CENTER);
        add(painelDireito, BorderLayout.EAST);

        carregarHospitais();
        limparFormulario();
    }

    private void adicionarCampo(JPanel painel, GridBagConstraints c, int linha, String rotulo, JComponent campo) {
        c.gridx = 0;
        c.gridy = linha;
        c.gridwidth = 1;
        c.weightx = 0;
        painel.add(new JLabel(rotulo), c);

        c.gridx = 1;
        c.weightx = 1;
        painel.add(campo, c);
    }

    // ============================================================
    // Carregamento / listagem
    // ============================================================

    private void carregarHospitais() {
        modeloTabela.setRowCount(0);
        for (Hospital h : listarHospitaisDoBackend()) {
            modeloTabela.addRow(new Object[]{
                    h.getId(),
                    h.getNome(),
                    h.getLatitude(),
                    h.getLongitude(),
                    h.getCapacidadeMaxima(),
                    h.getOcupacaoAtual(),
                    h.isDisponivel() ? "DISPONÍVEL" : "LOTADO"
            });
        }
    }

    private void carregarSelecaoNoFormulario() {
        int linha = tabelaHospitais.getSelectedRow();
        if (linha < 0) return;

        int id = (Integer) modeloTabela.getValueAt(linha, 0);
        Hospital selecionado = buscarHospitalPorId(id);
        if (selecionado == null) return;

        hospitalEmEdicao = selecionado;
        txtId.setText(String.valueOf(selecionado.getId()));
        txtNome.setText(selecionado.getNome());
        txtLatitude.setText(String.valueOf(selecionado.getLatitude()));
        txtLongitude.setText(String.valueOf(selecionado.getLongitude()));
        txtCapacidadeMaxima.setText(String.valueOf(selecionado.getCapacidadeMaxima()));
        txtOcupacaoAtual.setText(String.valueOf(selecionado.getOcupacaoAtual()));
    }

    // ============================================================
    // Ações dos botões
    // ============================================================

    private void aoClicarNovo(ActionEvent e) {
        limparFormulario();
        txtNome.requestFocus();
    }

    private void aoClicarSalvar(ActionEvent e) {
        String nome = txtNome.getText().trim();
        if (nome.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do hospital.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double latitude, longitude;
        int capacidadeMaxima, ocupacaoAtual;
        try {
            latitude = Double.parseDouble(txtLatitude.getText().trim());
            longitude = Double.parseDouble(txtLongitude.getText().trim());
            capacidadeMaxima = Integer.parseInt(txtCapacidadeMaxima.getText().trim());
            ocupacaoAtual = txtOcupacaoAtual.getText().trim().isEmpty()
                    ? 0
                    : Integer.parseInt(txtOcupacaoAtual.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Coordenadas e capacidades devem ser numéricas.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (ocupacaoAtual > capacidadeMaxima) {
            JOptionPane.showMessageDialog(this, "Ocupação atual não pode exceder a capacidade máxima.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (hospitalEmEdicao == null) {
            // ---- Novo hospital ----
            int novoId = gerarProximoId();
            Hospital novo = new Hospital(novoId, nome, latitude, longitude, capacidadeMaxima, ocupacaoAtual);
            cadastrarHospitalNoBackend(novo);
        } else {
            // ---- Edição ----
            hospitalEmEdicao.setNome(nome);
            hospitalEmEdicao.setLatitude(latitude);
            hospitalEmEdicao.setLongitude(longitude);
            hospitalEmEdicao.setCapacidadeMaxima(capacidadeMaxima);
            hospitalEmEdicao.setOcupacaoAtual(ocupacaoAtual);
        }

        carregarHospitais();
        limparFormulario();
    }

    private void aoClicarRemover(ActionEvent e) {
        if (hospitalEmEdicao == null) {
            JOptionPane.showMessageDialog(this, "Selecione um hospital na lista para remover.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Remover o hospital \"" + hospitalEmEdicao.getNome() + "\"? Isso também remove suas arestas do grafo.",
                "Confirmar remoção",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirmacao == JOptionPane.YES_OPTION) {
            removerHospitalDoBackend(hospitalEmEdicao);
            carregarHospitais();
            limparFormulario();
        }
    }

    private void limparFormulario() {
        hospitalEmEdicao = null;
        txtId.setText("(novo)");
        txtNome.setText("");
        txtLatitude.setText("");
        txtLongitude.setText("");
        txtCapacidadeMaxima.setText("");
        txtOcupacaoAtual.setText("0");
        tabelaHospitais.clearSelection();
    }

    /**
     * Gera o próximo id inteiro disponível olhando os vértices já existentes no grafo.
     * TODO: se GrafoCidade já tiver um gerador de IDs próprio (ex: contador interno,
     * usado também para Base SAMU/Bairro/Cruzamento/Paciente), troque esta implementação
     * por uma chamada a ele, para não haver risco de colisão de IDs entre tipos de vértice.
     */
    private int gerarProximoId() {
        int maiorId = 0;
        for (Vertice v : AppContext.getInstancia().getGrafo().getVertices()) {
            maiorId = Math.max(maiorId, v.getId());
        }
        return maiorId + 1;
    }

    // ============================================================
    // ===== INTEGRAÇÃO COM O BACKEND =====
    // ============================================================

    private List<Hospital> listarHospitaisDoBackend() {
        return AppContext.getInstancia().getGrafo().getHospitais();
    }

    private Hospital buscarHospitalPorId(int id) {
        Vertice v = AppContext.getInstancia().getGrafo().getVerticePorId(id);
        return (v instanceof Hospital) ? (Hospital) v : null;
    }

    private void cadastrarHospitalNoBackend(Hospital hospital) {
        boolean adicionado = AppContext.getInstancia().getGrafo().addVertice(hospital);
        if (!adicionado) {
            JOptionPane.showMessageDialog(this,
                    "Não foi possível cadastrar: já existe um vértice com este ID.",
                    "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerHospitalDoBackend(Hospital hospital) {
        // Requer o método removerVertice(Vertice) em GrafoCidade — ver instruções enviadas junto com esta tela.
        AppContext.getInstancia().getGrafo().removerVertice(hospital);
    }

    // ============================================================
    // Execução isolada da tela (útil para testar sem o Main completo)
    // ============================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TelaCadastroHospital().setVisible(true));
    }
}