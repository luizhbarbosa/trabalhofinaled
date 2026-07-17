package telas;

import grafo.Ambulancia;
import grafo.Dijkstra;
import grafo.GrafoCidade;
import grafo.Hospital;
import grafo.NivelUrgencia;
import grafo.Paciente;
import grafo.SistemaEmergencia;
import grafo.TipoVertice;
import grafo.Vertice;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TelaAtendimento extends JDialog {

    private JComboBox<Vertice> cbLocalizacao;
    private JComboBox<String> cbUrgencia;
    private JTextField txtDescricao;
    private JLabel lblEtaPreview;

    private JButton btnDespachar;
    private JButton btnCancelar;

    private SistemaEmergencia sistema;
    private GrafoCidade grafo;
    private List<Ambulancia> ambulancias;
    private TelaPrincipal telaPrincipal;

    public TelaAtendimento(TelaPrincipal parent, SistemaEmergencia sistema, GrafoCidade grafo, List<Ambulancia> ambulancias) {
        super(parent, "Nova Ocorrência - Despacho SAMU", true);
        this.telaPrincipal = parent;
        this.sistema = sistema;
        this.grafo = grafo;
        this.ambulancias = ambulancias;

        setSize(520, 350);
        setLocationRelativeTo(parent);
        setResizable(false);

        criarComponentes();
        calcularPreview();
    }

    private void criarComponentes() {
        setLayout(new BorderLayout());

        JPanel painelForm = new JPanel(new GridBagLayout());
        painelForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);

        // 1. Local da Ocorrência
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Local da Ocorrência:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbLocalizacao = new JComboBox<>(carregarLocaisAtendimento());
        // Renderer para mostrar apenas o nome do local
        cbLocalizacao.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, 
                    value instanceof Vertice ? ((Vertice) value).getNome() : value, 
                    index, isSelected, cellHasFocus);
                return this;
            }
        });
        cbLocalizacao.setPreferredSize(new Dimension(220, 28));
        cbLocalizacao.addActionListener(e -> calcularPreview());
        painelForm.add(cbLocalizacao, gbc);

        // 2. Nível de Urgência
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Nível de Urgência:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cbUrgencia = new JComboBox<>(new String[]{"Alta (Vermelho)", "Média (Amarelo)", "Baixa (Verde)"});
        cbUrgencia.setPreferredSize(new Dimension(220, 28));
        painelForm.add(cbUrgencia, gbc);

        // 3. Descrição da Emergência
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        painelForm.add(new JLabel("Descrição / Sintomas:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        txtDescricao = new JTextField("Acidente de trânsito / Lesão grave");
        txtDescricao.setPreferredSize(new Dimension(220, 28));
        painelForm.add(txtDescricao, gbc);

        // 4. Preview do ETA
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        painelForm.add(new JLabel("⏱️ Previsão de Chegada:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        lblEtaPreview = new JLabel("Selecione o local para calcular...");
        lblEtaPreview.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblEtaPreview.setForeground(new Color(40, 100, 40));
        painelForm.add(lblEtaPreview, gbc);

        add(painelForm, BorderLayout.CENTER);

        // Painel inferior de botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnDespachar = new JButton("🚨 Despachar Ambulância");
        btnDespachar.setBackground(new Color(220, 53, 69));
        btnDespachar.setForeground(Color.WHITE);
        btnDespachar.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnDespachar.setPreferredSize(new Dimension(220, 32));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(110, 32));

        btnDespachar.addActionListener(e -> realizarDespacho());
        btnCancelar.addActionListener(e -> dispose());

        painelBotoes.add(btnDespachar);
        painelBotoes.add(btnCancelar);
        add(painelBotoes, BorderLayout.SOUTH);
    }

    private void calcularPreview() {
        Vertice local = (Vertice) cbLocalizacao.getSelectedItem();
        if (local == null) {
            lblEtaPreview.setText("Selecione o local para calcular...");
            return;
        }

        double menorCusto = Double.MAX_VALUE;
        Ambulancia melhorAmb = null;

        for (Ambulancia amb : ambulancias) {
            if (amb.isDisponivel() && amb.getLocalizacaoAtual() != null) {
                Dijkstra.Resultado res = Dijkstra.encontrarMenorCaminho(
                        grafo, amb.getLocalizacaoAtual(), local);
                if (res.temCaminho() && res.getCustoTotal() < menorCusto) {
                    menorCusto = res.getCustoTotal();
                    melhorAmb = amb;
                }
            }
        }

        if (melhorAmb != null) {
            String eta = sistema != null ? sistema.estimarTempoChegada(menorCusto) : String.format("%.0f min", menorCusto);
            lblEtaPreview.setText("🚑 Ambulância #" + melhorAmb.getId() + " em ~" + eta);
            lblEtaPreview.setForeground(new Color(40, 100, 40));
        } else {
            lblEtaPreview.setText("⚠️ Nenhuma ambulância disponível no momento");
            lblEtaPreview.setForeground(new Color(180, 80, 0));
        }
    }

    private Vertice[] carregarLocaisAtendimento() {
        if (grafo == null) return new Vertice[0];
        
        List<Vertice> locais = new ArrayList<>();
        for (Vertice v : grafo.getVertices()) {
            if (v.getTipo() == TipoVertice.BAIRRO || v.getTipo() == TipoVertice.CRUZAMENTO) {
                locais.add(v);
            }
        }
        if (locais.isEmpty()) return grafo.getVertices().toArray(new Vertice[0]);
        
        // Ordena em ordem alfabética pelo nome
        locais.sort((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()));
        
        return locais.toArray(new Vertice[0]);
    }

    private void realizarDespacho() {
        Vertice localAcidente = (Vertice) cbLocalizacao.getSelectedItem();
        String urgencia = (String) cbUrgencia.getSelectedItem();
        String descricao = txtDescricao.getText().trim();

        if (localAcidente == null) {
            JOptionPane.showMessageDialog(this, "Selecione o local da ocorrência!", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // PASSO 1: Criar paciente e registrar ocorrência via SistemaEmergencia
        NivelUrgencia nivel = NivelUrgencia.MEDIA;
        if (urgencia.startsWith("Alta")) nivel = NivelUrgencia.ALTA;
        else if (urgencia.startsWith("Baixa")) nivel = NivelUrgencia.BAIXA;

        int pacienteId = 100 + (int)(Math.random() * 9000);
        Paciente paciente = new Paciente(pacienteId, "Paciente", 
                localAcidente.getLatitude(), localAcidente.getLongitude(),
                nivel, descricao);

        SistemaEmergencia.AtendimentoResultado atendimento = null;
        if (sistema != null) {
            atendimento = sistema.registrarOcorrencia(paciente);
        }

        Ambulancia ambulanciaEscolhida = null;
        Hospital hospitalEscolhido = null;
        List<Vertice> rotaAteAcidente = null;
        List<Vertice> rotaAteHospital = null;
        double custoAmbulancia = 0;
        double custoHospital = 0;

        if (atendimento != null && atendimento.isSucesso()) {
            ambulanciaEscolhida = atendimento.getAmbulancia();
            List<Vertice> caminhoHospital = atendimento.getRotaHospital().getCaminho();
            hospitalEscolhido = (Hospital) caminhoHospital.get(caminhoHospital.size() - 1);
            rotaAteAcidente = atendimento.getRotaAmbulancia().getCaminho();
            rotaAteHospital = atendimento.getRotaHospital().getCaminho();
            custoAmbulancia = atendimento.getRotaAmbulancia().getCustoTotal();
            custoHospital = atendimento.getRotaHospital().getCustoTotal();
        }

        // Fallback com Dijkstra
        if (ambulanciaEscolhida == null) {
            double menorCusto = Double.MAX_VALUE;
            for (Ambulancia amb : ambulancias) {
                if (amb.isDisponivel() && amb.getLocalizacaoAtual() != null) {
                    Dijkstra.Resultado resultado = Dijkstra.encontrarMenorCaminho(
                            grafo, amb.getLocalizacaoAtual(), localAcidente);
                    if (resultado.temCaminho() && resultado.getCustoTotal() < menorCusto) {
                        menorCusto = resultado.getCustoTotal();
                        ambulanciaEscolhida = amb;
                        rotaAteAcidente = resultado.getCaminho();
                        custoAmbulancia = resultado.getCustoTotal();
                    }
                }
            }

            double menorCustoHosp = Double.MAX_VALUE;
            for (Hospital h : grafo.getHospitais()) {
                if (h.isDisponivel()) {
                    Dijkstra.Resultado resultado = Dijkstra.encontrarMenorCaminho(
                            grafo, localAcidente, h);
                    if (resultado.temCaminho() && resultado.getCustoTotal() < menorCustoHosp) {
                        menorCustoHosp = resultado.getCustoTotal();
                        hospitalEscolhido = h;
                        rotaAteHospital = resultado.getCaminho();
                        custoHospital = resultado.getCustoTotal();
                    }
                }
            }
        }

        if (ambulanciaEscolhida == null) {
            JOptionPane.showMessageDialog(this, 
                "CRÍTICO: Nenhuma ambulância disponível no momento!\nTodas estão em atendimento ou desativadas.", 
                "Falta de Recursos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (hospitalEscolhido == null) {
            JOptionPane.showMessageDialog(this, 
                "CRÍTICO: Todos os hospitais da rede estão com lotação máxima (100%)!\nImpossível admitir novos pacientes.", 
                "Colapso na Rede Hospitalar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // PASSO 3: Preparar o Atendimento e Montar a Rota Completa
        ambulanciaEscolhida.iniciarAtendimento();
        hospitalEscolhido.admitirPaciente();

        Vertice baseOrigem = ambulanciaEscolhida.getLocalizacaoAtual();

        if (rotaAteAcidente == null) {
            Dijkstra.Resultado res = Dijkstra.encontrarMenorCaminho(grafo, baseOrigem, localAcidente);
            rotaAteAcidente = res.getCaminho();
            custoAmbulancia = res.getCustoTotal();
        }
        if (rotaAteHospital == null) {
            Dijkstra.Resultado res = Dijkstra.encontrarMenorCaminho(grafo, localAcidente, hospitalEscolhido);
            rotaAteHospital = res.getCaminho();
            custoHospital = res.getCustoTotal();
        }

        List<Vertice> rotaCompleta = new ArrayList<>();
        if (rotaAteAcidente != null) rotaCompleta.addAll(rotaAteAcidente);
        if (rotaAteHospital != null && rotaAteHospital.size() > 1) {
            for (int i = 1; i < rotaAteHospital.size(); i++) {
                rotaCompleta.add(rotaAteHospital.get(i));
            }
        }

        if (rotaCompleta.isEmpty()) {
            rotaCompleta.add(baseOrigem);
            rotaCompleta.add(localAcidente);
            rotaCompleta.add(hospitalEscolhido);
        }

        // Gera ETA formatado
        String etaAmbulancia = sistema != null ? sistema.estimarTempoChegada(custoAmbulancia) : String.format("%.0f min", custoAmbulancia);
        double custoTotal = custoAmbulancia + custoHospital;
        String etaTotal = sistema != null ? sistema.estimarTempoChegada(custoTotal) : String.format("%.0f min", custoTotal);

        // PASSO 4: Salva a rota no mapa (destaca a rota ANTES da ambulância começar a se mover)
        telaPrincipal.definirRotaOcorrencia(new ArrayList<>(rotaCompleta));

        // PASSO 5: Log com ETA
        telaPrincipal.adicionarLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "titulo");
        telaPrincipal.adicionarLog("🚨 OCORRÊNCIA DISPARADA!", "alerta");
        telaPrincipal.adicionarLog("   Local: " + localAcidente.getNome() + " (" + urgencia.split(" ")[0] + ")", "info");
        telaPrincipal.adicionarLog("   🚑 Ambulância #" + ambulanciaEscolhida.getId() + " saindo de: " + baseOrigem.getNome(), "info");
        telaPrincipal.adicionarLog("   ⏱️ ETA até o paciente: " + etaAmbulancia, "sucesso");
        telaPrincipal.adicionarLog("   🏥 Destino: " + hospitalEscolhido.getNome() + " (ETA total: ~" + etaTotal + ")", "info");
        telaPrincipal.adicionarLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", "titulo");

        // Guarda a base de origem para retorno após o atendimento
        final Vertice baseOrigemFinal = baseOrigem;
        final Hospital hospFinal = hospitalEscolhido;
        final Ambulancia ambFinal = ambulanciaEscolhida;

        telaPrincipal.animarDespacho(ambulanciaEscolhida, rotaCompleta, () -> {
            telaPrincipal.adicionarLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "titulo");
            telaPrincipal.adicionarLog("✅ RESGATE CONCLUÍDO!", "sucesso");
            telaPrincipal.adicionarLog("   🚑 Ambulância #" + ambFinal.getId() + " chegou em: " + hospFinal.getNome(), "info");
            telaPrincipal.adicionarLog("   🏥 Vagas restantes: " + (hospFinal.getCapacidadeMaxima() - hospFinal.getOcupacaoAtual()), "info");
            telaPrincipal.adicionarLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", "titulo");
            
            // Após entrega, calcula rota de volta para a base de origem
            Dijkstra.Resultado rotaVolta = Dijkstra.encontrarMenorCaminho(
                    grafo, hospFinal, baseOrigemFinal);
            
            if (rotaVolta.temCaminho() && rotaVolta.getCaminho().size() > 1) {
                String etaVolta = sistema != null ? sistema.estimarTempoChegada(rotaVolta.getCustoTotal()) : 
                    String.format("%.0f min", rotaVolta.getCustoTotal());
                
                telaPrincipal.adicionarLog("🔄 Ambulância #" + ambFinal.getId() + " retornando à base: " + baseOrigemFinal.getNome(), "info");
                telaPrincipal.adicionarLog("   ⏱️ ETA de retorno: ~" + etaVolta, "info");
                
                // Remove o destaque azul da rota antes de iniciar o retorno
                telaPrincipal.getMapa().setRotaDestacada(null);
                
                telaPrincipal.animarDespacho(ambFinal, rotaVolta.getCaminho(), () -> {
                    ambFinal.setLocalizacaoAtual(baseOrigemFinal);
                    ambFinal.finalizarAtendimento();
                    
                    telaPrincipal.adicionarLog("🏠 Ambulância #" + ambFinal.getId() + " retornou à base: " + baseOrigemFinal.getNome(), "sucesso");
                    telaPrincipal.adicionarLog("   ✅ Ambulância disponível para novas ocorrências.", "sucesso");
                    
                    telaPrincipal.getMapa().repaint();
                });
            } else {
                // Se não conseguir calcular rota de volta, marca como disponível onde está
                ambFinal.setLocalizacaoAtual(hospFinal);
                ambFinal.finalizarAtendimento();
                telaPrincipal.adicionarLog("⚠️ Ambulância #" + ambFinal.getId() + " sem rota de retorno à base. Permanece em: " + hospFinal.getNome(), "alerta");
                telaPrincipal.getMapa().repaint();
            }
            
            telaPrincipal.getMapa().repaint();
        });

        dispose();
    }
}