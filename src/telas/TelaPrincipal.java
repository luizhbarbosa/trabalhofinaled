package telas;

import grafo.Ambulancia;
import grafo.Aresta;
import grafo.Dijkstra;
import grafo.GrafoCidade;
import grafo.Hospital;
import grafo.RecalculoRota;
import grafo.SeedDados;
import grafo.SistemaEmergencia;
import grafo.Vertice;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;


public class TelaPrincipal extends JFrame {

    private JTextPane txtLog;
    private StyledDocument docLog;

    private JButton btnIniciar;
    private JButton btnNovaOcorrencia;
    private JButton btnRecalcular;
    private JButton btnCadastrarHospital;
    private JButton btnCadastrarVia;

    private PainelMapa mapa;
    
    private List<Ambulancia> ambulancias = new ArrayList<>();
    private SistemaEmergencia sistema;

    // Guarda a última rota para destacar no mapa
    private List<Vertice> ultimaRotaCompleta = null;

    public TelaPrincipal() {
        setTitle("Sistema de Emergência SAMU");
        setSize(1100, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.sistema = new SistemaEmergencia();

        criarTela();
        setVisible(true);
    }

    public PainelMapa getMapa() {
        return mapa;
    }

    private void criarTela() {
        setLayout(new BorderLayout());

        // Painel superior
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        painelSuperior.setBackground(new Color(30, 45, 60));

        btnIniciar = new JButton("▶️ Iniciar");
        btnNovaOcorrencia = new JButton("🆘 Ocorrência");
        btnRecalcular = new JButton("🔄 Recalcular");
        btnCadastrarHospital = new JButton("🏥 +Hospital");
        btnCadastrarVia = new JButton("🛣️ +Via");

        // Estilo dos botões
        Font btnFont = new Font("Segoe UI", Font.BOLD, 12);
        Color btnFg = Color.WHITE;
        Color btnBg = new Color(50, 75, 100);
        Dimension btnSize = new Dimension(160, 32);

        for (JButton btn : new JButton[]{btnIniciar, btnNovaOcorrencia, btnRecalcular, btnCadastrarHospital, btnCadastrarVia}) {
            btn.setFont(btnFont);
            btn.setForeground(btnFg);
            btn.setBackground(btnBg);
            btn.setPreferredSize(btnSize);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 100, 130), 1),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        }

        // Destaque para o botão de recalcular
        btnNovaOcorrencia.setBackground(new Color(180, 40, 50));
        btnRecalcular.setBackground(new Color(200, 130, 20));

        btnIniciar.addActionListener(e -> iniciarSimulacao());
        btnNovaOcorrencia.addActionListener(e -> abrirTelaAtendimento());
        btnRecalcular.addActionListener(e -> recalcularRota());
        btnCadastrarHospital.addActionListener(e -> abrirTelaCadastroHospital());
        btnCadastrarVia.addActionListener(e -> abrirTelaCadastroVia());

        painelSuperior.add(btnIniciar);
        painelSuperior.add(btnNovaOcorrencia);
        painelSuperior.add(btnRecalcular);
        painelSuperior.add(btnCadastrarHospital);
        painelSuperior.add(btnCadastrarVia);

        add(painelSuperior, BorderLayout.NORTH);

        // Área do mapa
        mapa = new PainelMapa(this);
        add(mapa, BorderLayout.CENTER);

        // Área de Log melhorada
        txtLog = new JTextPane();
        txtLog.setEditable(false);
        docLog = txtLog.getStyledDocument();

        // Estilos do log
        DefaultStyledDocument doc = (DefaultStyledDocument) docLog;
        StyleContext sc = new StyleContext();
        
        Style estiloTitulo = sc.addStyle("titulo", null);
        StyleConstants.setFontSize(estiloTitulo, 13);
        StyleConstants.setBold(estiloTitulo, true);
        StyleConstants.setForeground(estiloTitulo, new Color(30, 60, 90));
        
        Style estiloNormal = sc.addStyle("normal", null);
        StyleConstants.setFontSize(estiloNormal, 12);
        StyleConstants.setForeground(estiloNormal, new Color(50, 50, 50));
        
        Style estiloAlerta = sc.addStyle("alerta", null);
        StyleConstants.setFontSize(estiloAlerta, 12);
        StyleConstants.setBold(estiloAlerta, true);
        StyleConstants.setForeground(estiloAlerta, new Color(180, 40, 40));
        
        Style estiloSucesso = sc.addStyle("sucesso", null);
        StyleConstants.setFontSize(estiloSucesso, 12);
        StyleConstants.setBold(estiloSucesso, true);
        StyleConstants.setForeground(estiloSucesso, new Color(30, 130, 50));
        
        Style estiloInfo = sc.addStyle("info", null);
        StyleConstants.setFontSize(estiloInfo, 12);
        StyleConstants.setForeground(estiloInfo, new Color(60, 100, 140));

        JScrollPane scroll = new JScrollPane(txtLog);
        scroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 200)),
            "📋 Log da Simulação",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(40, 60, 80)
        ));
        scroll.setPreferredSize(new Dimension(380, 0));
        scroll.setBackground(new Color(248, 250, 252));

        add(scroll, BorderLayout.EAST);
    }

    /**
     * Define a rota da ocorrência atual para destacar no mapa.
     * Chamado ANTES da animação começar para que a rota apareça primeiro.
     */
    public void definirRotaOcorrencia(java.util.List<Vertice> rota) {
        this.ultimaRotaCompleta = rota != null ? new ArrayList<>(rota) : null;
        if (this.ultimaRotaCompleta != null) {
            mapa.setRotaDestacada(this.ultimaRotaCompleta);
        }
    }

    /**
     * Anima o deslocamento da ambulância percorrendo passo a passo uma lista de vértices no mapa.
     */
    public void animarDespacho(Ambulancia amb, java.util.List<Vertice> rotaCompleta, Runnable aoTerminar) {
        animarDespacho(amb, rotaCompleta, aoTerminar, null);
    }

    public void animarDespacho(Ambulancia amb, java.util.List<Vertice> rotaCompleta, Runnable aoTerminar, Vertice localPaciente) {
        if (rotaCompleta == null || rotaCompleta.size() < 2) {
            if (aoTerminar != null) aoTerminar.run();
            return;
        }

        // Guarda a rota para destacar no mapa
        this.ultimaRotaCompleta = new ArrayList<>(rotaCompleta);
        mapa.setRotaDestacada(this.ultimaRotaCompleta);

        javax.swing.Timer timer = new javax.swing.Timer(25, null);
        boolean[] pacienteRemovido = {false};

        timer.addActionListener(new java.awt.event.ActionListener() {
            int indiceTrecho = 0;
            double progresso = 0.0;
            final double VELOCIDADE = 0.05;
            final int indicePaciente = (localPaciente != null) ? rotaCompleta.indexOf(localPaciente) : -1;

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Vertice origem = rotaCompleta.get(indiceTrecho);
                Vertice destino = rotaCompleta.get(indiceTrecho + 1);

                double latAtual = origem.getLatitude() + (destino.getLatitude() - origem.getLatitude()) * progresso;
                double lonAtual = origem.getLongitude() + (destino.getLongitude() - origem.getLongitude()) * progresso;

                amb.setPosicaoVisual(latAtual, lonAtual);
                mapa.repaint();

                // Remove o bonequinho quando a ambulância passa pelo paciente
                if (!pacienteRemovido[0] && indicePaciente >= 0 && indiceTrecho >= indicePaciente) {
                    pacienteRemovido[0] = true;
                    mapa.removerPaciente(localPaciente);
                    adicionarLog("🚑 Ambulância #" + amb.getId() + " chegou ao paciente. Seguindo para o hospital...", "info");
                }

                progresso += VELOCIDADE;

                if (progresso >= 1.0) {
                    progresso = 0.0;
                    indiceTrecho++;

                    if (indiceTrecho >= rotaCompleta.size() - 1) {
                        timer.stop();
                        
                        Vertice destinoFinal = rotaCompleta.get(rotaCompleta.size() - 1);
                        amb.setLocalizacaoAtual(destinoFinal);
                        mapa.repaint();

                        if (aoTerminar != null) aoTerminar.run();
                    }
                }
            }
        });

        timer.start();
    }

    // ==========================================================
    // FUNCIONALIDADE: RECALCULAR ROTA
    // ==========================================================
    private void recalcularRota() {
        if (mapa.getGrafo() == null || mapa.getGrafo().getVertices().isEmpty()) {
            adicionarLog("⚠️ Inicie a simulação primeiro!", "alerta");
            return;
        }

        // Encontra a ambulância que está em atendimento ou a última despachada
        Ambulancia amb = mapa.getUltimaAmbulanciaDespachada();
        if (amb == null || amb.isDisponivel()) {
            adicionarLog("⚠️ Nenhuma ambulância em atendimento para recalcular rota.", "alerta");
            return;
        }

        // Pega a última rota para ter referência de destino
        if (ultimaRotaCompleta == null || ultimaRotaCompleta.size() < 2) {
            adicionarLog("⚠️ Nenhuma rota anterior disponível para recalcular.", "alerta");
            return;
        }

        Vertice destino = ultimaRotaCompleta.get(ultimaRotaCompleta.size() - 1);
        Vertice posicaoAtual = amb.getLocalizacaoAtual();

        // Verifica se existe alguma via bloqueada que justifique o recálculo
        boolean temBloqueio = false;
        for (Aresta a : mapa.getGrafo().getArestas()) {
            if (a.estaBloqueada()) {
                temBloqueio = true;
                break;
            }
        }

        if (!temBloqueio) {
            // Mesmo sem bloqueio, faz o recálculo para verificar se há rota melhor
            Dijkstra.Resultado novaRota = Dijkstra.encontrarMenorCaminho(
                    mapa.getGrafo(), posicaoAtual, destino);

            if (novaRota.temCaminho()) {
                double custoAtual = calcularCustoRota(ultimaRotaCompleta, posicaoAtual);
                
                if (novaRota.getCustoTotal() < custoAtual) {
                    // Rota melhor encontrada (talvez um congestionamento foi liberado)
                    this.ultimaRotaCompleta = new ArrayList<>(novaRota.getCaminho());
                    mapa.setRotaDestacada(this.ultimaRotaCompleta);
                    
                    String eta = sistema.estimarTempoChegada(novaRota.getCustoTotal());
                    adicionarLog("🔄 ROTA OTIMIZADA: Nova rota encontrada! ETA: ~" + eta, "sucesso");
                    
                    // Re-anima a ambulância na nova rota (a partir da posição atual até o destino)
                    List<Vertice> novoTrecho = novaRota.getCaminho();
                    animarDespacho(amb, novoTrecho, () -> {
                        adicionarLog("✅ Ambulância #" + amb.getId() + " chegou ao destino pela nova rota.", "sucesso");
                    });
                } else {
                    adicionarLog("✅ Rota atual já é a mais eficiente. Nenhuma alteração necessária.", "info");
                }
            } else {
                adicionarLog("❌ VIAS BLOQUEADAS: Nenhuma alternativa encontrada para o destino.", "alerta");
            }
            return;
        }

        // Se há bloqueio, usa o RecalculoRota oficial
        adicionarLog("🔄 Verificando impacto de bloqueios na rota...", "info");
        
        // Procura a primeira aresta bloqueada na rota atual
        Aresta arestaBloqueada = null;
        for (Aresta a : mapa.getGrafo().getArestas()) {
            if (a.estaBloqueada() && rotaContemAresta(ultimaRotaCompleta, a)) {
                arestaBloqueada = a;
                break;
            }
        }

        if (arestaBloqueada != null) {
            RecalculoRota.Resultado resultado = RecalculoRota.bloquearViaERecalcular(
                    mapa.getGrafo(), arestaBloqueada, ultimaRotaCompleta, destino, posicaoAtual);

            if (resultado.temCaminho()) {
                this.ultimaRotaCompleta = new ArrayList<>(resultado.getCaminho());
                mapa.setRotaDestacada(this.ultimaRotaCompleta);
                
                adicionarLog(resultado.getMensagem(), resultado.isRotaRecalculada() ? "sucesso" : "info");
                
                if (resultado.isRotaRecalculada()) {
                    // Re-anima na nova rota
                    List<Vertice> novoTrecho = resultado.getCaminho();
                    animarDespacho(amb, novoTrecho, () -> {
                        adicionarLog("✅ Ambulância #" + amb.getId() + " completou o percurso alternativo.", "sucesso");
                    });
                }
            } else {
                adicionarLog(resultado.getMensagem(), "alerta");
            }
        } else {
            // Nenhuma aresta bloqueada na rota atual, mas recalcula mesmo assim
            Dijkstra.Resultado novaRota = Dijkstra.encontrarMenorCaminho(
                    mapa.getGrafo(), posicaoAtual, destino);
            
            if (novaRota.temCaminho()) {
                this.ultimaRotaCompleta = new ArrayList<>(novaRota.getCaminho());
                mapa.setRotaDestacada(this.ultimaRotaCompleta);
                String eta = sistema.estimarTempoChegada(novaRota.getCustoTotal());
                adicionarLog("🔄 Rota recalculada. ETA atual: ~" + eta, "info");
            } else {
                adicionarLog("❌ VIAS BLOQUEADAS: Nenhuma alternativa encontrada para o destino.", "alerta");
            }
        }
    }

    private double calcularCustoRota(List<Vertice> rota, Vertice atePonto) {
        if (rota == null || rota.size() < 2) return Double.POSITIVE_INFINITY;
        
        double custo = 0;
        boolean encontrou = false;
        for (int i = 0; i < rota.size() - 1; i++) {
            if (rota.get(i).equals(atePonto)) encontrou = true;
            if (encontrou) {
                Vertice origem = rota.get(i);
                Vertice destino = rota.get(i + 1);
                double peso = mapa.getGrafo().getArestas().stream()
                    .filter(a -> a.getOrigem().equals(origem) && a.getDestino().equals(destino))
                    .mapToDouble(Aresta::getPesoEfetivo)
                    .findFirst()
                    .orElse(Double.POSITIVE_INFINITY);
                custo += peso;
            }
        }
        return custo;
    }

    private boolean rotaContemAresta(List<Vertice> rota, Aresta aresta) {
        if (rota == null || rota.size() < 2) return false;
        for (int i = 0; i < rota.size() - 1; i++) {
            if (rota.get(i).equals(aresta.getOrigem()) && rota.get(i + 1).equals(aresta.getDestino())) {
                return true;
            }
        }
        return false;
    }

    private void iniciarSimulacao() {
        adicionarLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", "titulo");
        adicionarLog("🚀 INICIANDO SIMULAÇÃO", "titulo");
        
        SeedDados.popular(sistema);
        
        this.ambulancias = sistema.getAmbulancias();
        mapa.setGrafo(sistema.getGrafo());
        mapa.setAmbulancias(this.ambulancias);

        adicionarLog("✅ Cidade carregada com sucesso!", "sucesso");
        adicionarLog("   📍 Vértices: " + sistema.getGrafo().getVertices().size(), "info");
        adicionarLog("   🛣️ Arestas: " + sistema.getGrafo().getArestas().size(), "info");
        adicionarLog("   🏥 Hospitais: " + sistema.getHospitais().size(), "info");
        adicionarLog("   🚑 Ambulâncias: " + this.ambulancias.size(), "info");
        adicionarLog("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", "titulo");
    }

    private void abrirTelaAtendimento() {
        if (mapa.getGrafo() == null || mapa.getGrafo().getVertices().isEmpty()) {
            adicionarLog("⚠️ Clique em 'Iniciar Simulação' antes de gerar ocorrências!", "alerta");
            JOptionPane.showMessageDialog(this, "Clique em 'Iniciar Simulação' antes de gerar ocorrências!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TelaAtendimento tela = new TelaAtendimento(this, this.sistema, mapa.getGrafo(), this.ambulancias);
        tela.setVisible(true);
    }

    private void abrirTelaCadastroHospital() {
        TelaCadastroHospital tela = new TelaCadastroHospital(this);
        tela.setVisible(true);

        Hospital h = tela.getHospitalCriado();
        
        if (h != null) {
            if (mapa.getGrafo() == null) {
                mapa.setGrafo(new GrafoCidade());
            }

            boolean adicionou = mapa.getGrafo().addVertice(h);
            boolean registrouNoSistema = sistema.cadastrarHospital(h);

            if (adicionou) {
                Vertice maisProximo = null;
                double menorDistancia = Double.MAX_VALUE;

                for (Vertice v : mapa.getGrafo().getVertices()) {
                    if (v.getId() != h.getId()) {
                        double dist = h.calcularDistancia(v);
                        if (dist < menorDistancia) {
                            menorDistancia = dist;
                            maisProximo = v;
                        }
                    }
                }

                if (maisProximo != null) {
                    double tempoEmMinutos = Math.max(0.1, (menorDistancia / 40.0) * 60.0);

                    grafo.Aresta viaIda = new grafo.Aresta(h, maisProximo, tempoEmMinutos, grafo.StatusVia.LIVRE, "Av. " + h.getNome());
                    grafo.Aresta viaVolta = new grafo.Aresta(maisProximo, h, tempoEmMinutos, grafo.StatusVia.LIVRE, "Av. " + h.getNome());
                    
                    mapa.getGrafo().addAresta(viaIda);
                    mapa.getGrafo().addAresta(viaVolta);
                    
                    adicionarLog("🛣️ Rua 'Av. " + h.getNome() + "' conectada a " + maisProximo.getNome() + " (" + String.format("%.1f", tempoEmMinutos) + " min)", "info");
                }

                adicionarLog("🏥 Hospital '" + h.getNome() + "' cadastrado com sucesso!", "sucesso");
                if (registrouNoSistema) {
                    adicionarLog("   → Registrado no sistema de emergência.", "info");
                }
                mapa.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "ID " + h.getId() + " já está em uso! Tente um número maior.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void abrirTelaCadastroVia() {
        if (mapa.getGrafo() == null || mapa.getGrafo().getVertices().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inicie a simulação ou cadastre pontos antes de criar ruas!", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TelaCadastroVia tela = new TelaCadastroVia(this, mapa.getGrafo());
        tela.setVisible(true);

        List<Aresta> novasVias = tela.getArestasCriadas();

        if (!novasVias.isEmpty()) {
            int adicionadas = 0;
            for (Aresta via : novasVias) {
                boolean sucesso = mapa.getGrafo().addAresta(via);
                if (sucesso) {
                    adicionadas++;
                }
            }

            if (adicionadas > 0) {
                Aresta exemplo = novasVias.get(0);
                adicionarLog("🛣️ Nova via: '" + exemplo.getNome() + "' (" + String.format("%.1f", exemplo.getPeso()) + " min)", "sucesso");
                adicionarLog("   → Conectando: " + exemplo.getOrigem().getNome() + " ↔ " + exemplo.getDestino().getNome(), "info");
                mapa.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Essa via já existe no sistema!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            adicionarLog("Cadastro de via cancelado.", "info");
        }
    }

    /**
     * Adiciona texto ao log com estilo baseado no conteúdo.
     */
    public void adicionarLog(String texto) {
        adicionarLog(texto, "normal");
    }

    public void adicionarLog(String texto, String estilo) {
        try {
            StyleContext sc = new StyleContext();
            Style style = sc.addStyle("dynamic", null);
            StyleConstants.setFontSize(style, 12);
            StyleConstants.setFontFamily(style, "Segoe UI");

            if ("titulo".equals(estilo)) {
                StyleConstants.setBold(style, true);
                StyleConstants.setFontSize(style, 13);
                StyleConstants.setForeground(style, new Color(30, 60, 90));
            } else if ("alerta".equals(estilo)) {
                StyleConstants.setBold(style, true);
                StyleConstants.setForeground(style, new Color(180, 40, 40));
            } else if ("sucesso".equals(estilo)) {
                StyleConstants.setBold(style, true);
                StyleConstants.setForeground(style, new Color(30, 130, 50));
            } else if ("info".equals(estilo)) {
                StyleConstants.setForeground(style, new Color(60, 100, 140));
            } else {
                StyleConstants.setForeground(style, new Color(50, 50, 50));
            }

            docLog.insertString(docLog.getLength(), texto + "\n", style);
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        } catch (Exception ex) {
            // Fallback
            txtLog.setText(txtLog.getText() + texto + "\n");
        }
    }
}