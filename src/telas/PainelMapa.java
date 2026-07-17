package telas;

import grafo.Ambulancia;
import grafo.Aresta;
import grafo.GrafoCidade;
import grafo.Hospital;
import grafo.StatusVia;
import grafo.TipoVertice;
import grafo.Vertice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class PainelMapa extends JPanel {

    private GrafoCidade grafo;
    private List<Ambulancia> ambulancias = new ArrayList<>();
    private TelaPrincipal telaPrincipal;
    private List<Vertice> rotaDestacada = new ArrayList<>(); // rota da última ocorrência
    private List<Vertice> pacientesNoMapa = new ArrayList<>(); // pacientes (bonequinhos) no mapa

    // Controles de Zoom e Navegação (Pan)
    private double fatorZoom = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;
    private int ultimoMouseX, ultimoMouseY;
    private boolean arrastando = false;

    // Paleta de cores moderna e profissional
    private final Color COR_FUNDO = new Color(244, 247, 246);
    private final Color COR_GRID = new Color(225, 230, 230);
    private final Color COR_TEXTO = new Color(40, 40, 40);
    private final Color COR_RUA_BORDAS = new Color(180, 185, 190);
    private final Color COR_RUA_LIVRE = new Color(255, 255, 255);
    private final Color COR_RUA_CONGESTIONADA = new Color(255, 193, 7);
    private final Color COR_RUA_BLOQUEADA = new Color(220, 53, 69);
    private final Color COR_ROTA = new Color(0, 120, 255, 180);

    public PainelMapa() {
        this(null);
    }

    public PainelMapa(TelaPrincipal telaPrincipal) {
        this.telaPrincipal = telaPrincipal;
        setBackground(COR_FUNDO);
        configurarControlesZoom();
    }

    public void setTelaPrincipal(TelaPrincipal telaPrincipal) {
        this.telaPrincipal = telaPrincipal;
    }

    public void setRotaDestacada(List<Vertice> rota) {
        this.rotaDestacada = rota != null ? rota : new ArrayList<>();
        repaint();
    }

    private void configurarControlesZoom() {
        setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 15));

        JPanel painelBotoes = new JPanel(new GridLayout(1, 3, 4, 0));
        painelBotoes.setBackground(new Color(255, 255, 255, 230));
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JButton btnZoomIn = criarBotaoZoom("+", "Aumentar Zoom");
        JButton btnZoomOut = criarBotaoZoom("-", "Diminuir Zoom");
        JButton btnReset = criarBotaoZoom("⌂", "Centralizar Mapa");

        btnZoomIn.addActionListener(e -> zoomIn());
        btnZoomOut.addActionListener(e -> zoomOut());
        btnReset.addActionListener(e -> resetZoom());

        painelBotoes.add(btnZoomIn);
        painelBotoes.add(btnZoomOut);
        painelBotoes.add(btnReset);
        add(painelBotoes);

        addMouseWheelListener(e -> {
            if (e.getPreciseWheelRotation() < 0) zoomIn();
            else zoomOut();
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                ultimoMouseX = e.getX();
                ultimoMouseY = e.getY();
                arrastando = false;
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                if (!arrastando) {
                    verificarCliqueEmViaOuHospital(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - ultimoMouseX;
                int dy = e.getY() - ultimoMouseY;
                if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
                    arrastando = true;
                }
                offsetX += dx;
                offsetY += dy;
                ultimoMouseX = e.getX();
                ultimoMouseY = e.getY();
                repaint();
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    // ==========================================================
    // DETECÇÃO DE CLIQUE NA RUA OU HOSPITAL
    // ==========================================================
    private void verificarCliqueEmViaOuHospital(int mouseX, int mouseY) {
        if (grafo == null || grafo.getVertices().isEmpty()) return;

        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (Vertice v : grafo.getVertices()) {
            minLat = Math.min(minLat, v.getLatitude());
            maxLat = Math.max(maxLat, v.getLatitude());
            minLon = Math.min(minLon, v.getLongitude());
            maxLon = Math.max(maxLon, v.getLongitude());
        }

        int margem = 60;
        int largura = getWidth() - margem * 2;
        int altura = getHeight() - margem * 2;

        // Primeiro verifica se clicou em um hospital (ícone maior, verifica primeiro)
        for (Vertice v : grafo.getVertices()) {
            if (v instanceof Hospital) {
                int x = converterX(v.getLongitude(), minLon, maxLon, largura, margem);
                int y = converterY(v.getLatitude(), minLat, maxLat, altura, margem);
                // Área de clique do hospital (~20px de raio)
                if (Math.abs(mouseX - x) < 20 && Math.abs(mouseY - y) < 20) {
                    abrirMenuHospital((Hospital) v);
                    return;
                }
            }
        }

        // Depois verifica clique em via
        Aresta arestaClicada = null;
        double menorDistanciaClique = 8.0;

        for (Aresta a : grafo.getArestas()) {
            int x1 = converterX(a.getOrigem().getLongitude(), minLon, maxLon, largura, margem);
            int y1 = converterY(a.getOrigem().getLatitude(), minLat, maxLat, altura, margem);
            int x2 = converterX(a.getDestino().getLongitude(), minLon, maxLon, largura, margem);
            int y2 = converterY(a.getDestino().getLatitude(), minLat, maxLat, altura, margem);

            double dist = Line2D.ptSegDist(x1, y1, x2, y2, mouseX, mouseY);
            if (dist < menorDistanciaClique) {
                menorDistanciaClique = dist;
                arestaClicada = a;
            }
        }

        if (arestaClicada != null) {
            abrirMenuAlterarStatus(arestaClicada);
        }
    }

    private void abrirMenuHospital(Hospital hospital) {
        String[] opcoes = {"Alterar Capacidade", "Alterar Ocupação", "Ver Detalhes", "Cancelar"};
        int escolha = JOptionPane.showOptionDialog(this,
                "🏥 " + hospital.getNome() + "\nCapacidade: " + hospital.getCapacidadeMaxima() +
                " | Ocupação: " + hospital.getOcupacaoAtual() + " | Vagas: " +
                (hospital.getCapacidadeMaxima() - hospital.getOcupacaoAtual()),
                "Gerenciar Hospital",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);

        if (escolha == 0) {
            String input = JOptionPane.showInputDialog(this,
                    "Nova capacidade máxima para " + hospital.getNome() + ":",
                    hospital.getCapacidadeMaxima());
            if (input != null) {
                try {
                    int novaCapacidade = Integer.parseInt(input.trim());
                    if (novaCapacidade > 0) {
                        hospital.setCapacidadeMaxima(novaCapacidade);
                        if (telaPrincipal != null) {
                            telaPrincipal.adicionarLog("🏥 Capacidade do " + hospital.getNome() + " alterada para " + novaCapacidade);
                        }
                        repaint();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (escolha == 1) {
            String input = JOptionPane.showInputDialog(this,
                    "Nova ocupação para " + hospital.getNome() + " (0 a " + hospital.getCapacidadeMaxima() + "):",
                    hospital.getOcupacaoAtual());
            if (input != null) {
                try {
                    int novaOcupacao = Integer.parseInt(input.trim());
                    if (novaOcupacao >= 0 && novaOcupacao <= hospital.getCapacidadeMaxima()) {
                        hospital.setOcupacaoAtual(novaOcupacao);
                        if (telaPrincipal != null) {
                            telaPrincipal.adicionarLog("🏥 Ocupação do " + hospital.getNome() + " alterada para " + novaOcupacao +
                                    "/" + hospital.getCapacidadeMaxima());
                        }
                        repaint();
                    } else {
                        JOptionPane.showMessageDialog(this, "Ocupação deve estar entre 0 e " + hospital.getCapacidadeMaxima(), "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Valor inválido!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else if (escolha == 2) {
            JOptionPane.showMessageDialog(this,
                    "🏥 " + hospital.getNome() + "\n" +
                    "   ID: " + hospital.getId() + "\n" +
                    "   Capacidade Máxima: " + hospital.getCapacidadeMaxima() + "\n" +
                    "   Ocupação Atual: " + hospital.getOcupacaoAtual() + "\n" +
                    "   Vagas Disponíveis: " + (hospital.getCapacidadeMaxima() - hospital.getOcupacaoAtual()) + "\n" +
                    "   Latitude: " + String.format("%.4f", hospital.getLatitude()) + "\n" +
                    "   Longitude: " + String.format("%.4f", hospital.getLongitude()),
                    "Detalhes do Hospital",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void abrirMenuAlterarStatus(Aresta aresta) {
        String nomeRua = aresta.getNome() != null ? aresta.getNome() : aresta.getOrigem().getNome() + " ↔ " + aresta.getDestino().getNome();
        
        String[] opcoes = {"Livre (Normal)", "Congestionada (Lenta)", "Bloqueada (Intransitável)", "Cancelar"};
        int escolha = JOptionPane.showOptionDialog(this,
                "Selecione o novo status para a rua:\n\n📍 " + nomeRua + "\nStatus Atual: " + aresta.getStatus().getDescricao(),
                "Alterar Tráfego da Via",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcoes,
                opcoes[0]);

        StatusVia novoStatus = null;
        if (escolha == 0) novoStatus = StatusVia.LIVRE;
        else if (escolha == 1) novoStatus = StatusVia.CONGESTIONADA;
        else if (escolha == 2) novoStatus = StatusVia.BLOQUEADA;

        if (novoStatus != null && novoStatus != aresta.getStatus()) {
            aresta.setStatus(novoStatus);

            for (Aresta volta : grafo.getArestas()) {
                if (volta.getOrigem().getId() == aresta.getDestino().getId() && volta.getDestino().getId() == aresta.getOrigem().getId()) {
                    volta.setStatus(novoStatus);
                }
            }

            if (telaPrincipal != null) {
                telaPrincipal.adicionarLog("⚠️ TRÂFEGO ALTERADO: '" + nomeRua + "'");
                telaPrincipal.adicionarLog(" -> Novo status: " + novoStatus.getDescricao().toUpperCase());
            }

            repaint();
        }
    }

    private JButton criarBotaoZoom(String texto, String tooltip) {
        JButton btn = new JButton(texto);
        btn.setToolTipText(tooltip);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusable(false);
        btn.setMargin(new Insets(2, 8, 2, 8));
        btn.setBackground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void zoomIn() {
        fatorZoom *= 1.25;
        if (fatorZoom > 5.0) fatorZoom = 5.0;
        repaint();
    }

    public void zoomOut() {
        fatorZoom /= 1.25;
        if (fatorZoom < 0.4) fatorZoom = 0.4;
        repaint();
    }

    public void resetZoom() {
        fatorZoom = 1.0;
        offsetX = 0;
        offsetY = 0;
        repaint();
    }

    public void setGrafo(GrafoCidade grafo) {
        this.grafo = grafo;
        repaint();
    }

    public GrafoCidade getGrafo() {
        return grafo;
    }

    public void adicionarVertice(Vertice v) {
        if (this.grafo != null) {
            this.grafo.addVertice(v);
            repaint();
        }
    }

    public void setAmbulancias(List<Ambulancia> ambulancias) {
        this.ambulancias = ambulancias;
        repaint();
    }

    /**
     * Adiciona um paciente (bonequinho) ao mapa para ser desenhado.
     */
    public void adicionarPaciente(Vertice paciente) {
        if (paciente != null && !pacientesNoMapa.contains(paciente)) {
            pacientesNoMapa.add(paciente);
            repaint();
        }
    }

    /**
     * Remove um paciente do mapa (quando o resgate é concluído).
     */
    public void removerPaciente(Vertice paciente) {
        pacientesNoMapa.remove(paciente);
        repaint();
    }

    /**
     * Limpa todos os pacientes do mapa.
     */
    public void limparPacientes() {
        pacientesNoMapa.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        desenharGrid(g2);

        if (grafo == null || grafo.getVertices().isEmpty()) {
            // Tela de boas-vindas mais bonita
            g2.setColor(new Color(40, 60, 80));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Dialog", Font.BOLD, 28));
            String titulo = "🚑 SISTEMA DE EMERGÊNCIA SAMU";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(titulo, (getWidth() - fm.stringWidth(titulo)) / 2, getHeight() / 2 - 40);
            
            g2.setFont(new Font("Dialog", Font.PLAIN, 18));
            String subtitulo = "Clique no botão 'Iniciar' para começar";
            fm = g2.getFontMetrics();
            g2.drawString(subtitulo, (getWidth() - fm.stringWidth(subtitulo)) / 2, getHeight() / 2 + 10);
            
            g2.setFont(new Font("Dialog", Font.PLAIN, 13));
            String dica = "💡 Dica: Após iniciar, clique nas ruas para alterar o tráfego ou nos hospitais para ajustar capacidade";
            fm = g2.getFontMetrics();
            g2.drawString(dica, (getWidth() - fm.stringWidth(dica)) / 2, getHeight() / 2 + 50);
            return;
        }

        double minLat = Double.MAX_VALUE, maxLat = -Double.MAX_VALUE;
        double minLon = Double.MAX_VALUE, maxLon = -Double.MAX_VALUE;

        for (Vertice v : grafo.getVertices()) {
            minLat = Math.min(minLat, v.getLatitude());
            maxLat = Math.max(maxLat, v.getLatitude());
            minLon = Math.min(minLon, v.getLongitude());
            maxLon = Math.max(maxLon, v.getLongitude());
        }

        int margem = 60;
        int largura = getWidth() - margem * 2;
        int altura = getHeight() - margem * 2;

        desenharRuas(g2, minLon, maxLon, minLat, maxLat, largura, altura, margem);
        desenharRotaDestacada(g2, minLon, maxLon, minLat, maxLat, largura, altura, margem);
        desenharVertices(g2, minLon, maxLon, minLat, maxLat, largura, altura, margem);
        desenharPacientes(g2, minLon, maxLon, minLat, maxLat, largura, altura, margem);
        desenharAmbulancias(g2, minLon, maxLon, minLat, maxLat, largura, altura, margem);
        desenharTextos(g2, minLon, maxLon, minLat, maxLat, largura, altura, margem);
        desenharLegenda(g2);
    }

    /**
     * Desenha os pacientes (bonequinhos) no mapa no local da ocorrência.
     */
    private void desenharPacientes(Graphics2D g2, double minLon, double maxLon, double minLat, double maxLat, int largura, int altura, int margem) {
        if (pacientesNoMapa == null || pacientesNoMapa.isEmpty()) return;

        for (Vertice paciente : pacientesNoMapa) {
            int x = converterX(paciente.getLongitude(), minLon, maxLon, largura, margem);
            int y = converterY(paciente.getLatitude(), minLat, maxLat, altura, margem);

            // Sombra
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillOval(x - 7, y + 8, 14, 4);

            // Corpo (camiseta vermelha)
            g2.setColor(new Color(220, 53, 69));
            g2.fillOval(x - 6, y - 4, 12, 14);

            // Cabeça (círculo)
            g2.setColor(new Color(255, 200, 150));
            g2.fillOval(x - 5, y - 12, 10, 10);

            // Olhos
            g2.setColor(Color.BLACK);
            g2.fillOval(x - 3, y - 9, 2, 2);
            g2.fillOval(x + 1, y - 9, 2, 2);

            // Braços (linhas)
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(255, 200, 150));
            g2.drawLine(x - 7, y + 1, x - 11, y + 6);   // braço esquerdo
            g2.drawLine(x + 7, y + 1, x + 11, y + 6);   // braço direito

            // Pernas (linhas)
            g2.setColor(new Color(40, 50, 80));
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x - 3, y + 9, x - 5, y + 15);   // perna esquerda
            g2.drawLine(x + 3, y + 9, x + 5, y + 15);   // perna direita

            // Círculo pulsante em volta (emergência)
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(220, 53, 69, 120));
            g2.drawOval(x - 12, y - 16, 24, 34);
        }
    }

    /**
     * Desenha a rota destacada (da última ocorrência) com linhas azuis e pontos.
     */
    private void desenharRotaDestacada(Graphics2D g2, double minLon, double maxLon, double minLat, double maxLat, int largura, int altura, int margem) {
        if (rotaDestacada == null || rotaDestacada.size() < 2) return;

        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(COR_ROTA);

        for (int i = 0; i < rotaDestacada.size() - 1; i++) {
            int x1 = converterX(rotaDestacada.get(i).getLongitude(), minLon, maxLon, largura, margem);
            int y1 = converterY(rotaDestacada.get(i).getLatitude(), minLat, maxLat, altura, margem);
            int x2 = converterX(rotaDestacada.get(i + 1).getLongitude(), minLon, maxLon, largura, margem);
            int y2 = converterY(rotaDestacada.get(i + 1).getLatitude(), minLat, maxLat, altura, margem);
            g2.drawLine(x1, y1, x2, y2);
        }

        // Pontos nos vértices da rota
        g2.setColor(new Color(0, 80, 200));
        for (Vertice v : rotaDestacada) {
            int x = converterX(v.getLongitude(), minLon, maxLon, largura, margem);
            int y = converterY(v.getLatitude(), minLat, maxLat, altura, margem);
            g2.fillOval(x - 4, y - 4, 8, 8);
        }
    }

    private void desenharGrid(Graphics2D g2) {
        g2.setColor(COR_GRID);
        g2.setStroke(new BasicStroke(1));
        int step = 40;
        for (int x = 0; x < getWidth(); x += step) g2.drawLine(x, 0, x, getHeight());
        for (int y = 0; y < getHeight(); y += step) g2.drawLine(0, y, getWidth(), y);
    }

    private void desenharRuas(Graphics2D g2, double minLon, double maxLon, double minLat, double maxLat, int largura, int altura, int margem) {
        // CAMADA 1: BORDAS
        for (Aresta a : grafo.getArestas()) {
            int x1 = converterX(a.getOrigem().getLongitude(), minLon, maxLon, largura, margem);
            int y1 = converterY(a.getOrigem().getLatitude(), minLat, maxLat, altura, margem);
            int x2 = converterX(a.getDestino().getLongitude(), minLon, maxLon, largura, margem);
            int y2 = converterY(a.getDestino().getLatitude(), minLat, maxLat, altura, margem);

            g2.setColor(COR_RUA_BORDAS);
            g2.setStroke(new BasicStroke(8.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x1, y1, x2, y2);
        }

        // CAMADA 2: INTERIOR
        for (Aresta a : grafo.getArestas()) {
            int x1 = converterX(a.getOrigem().getLongitude(), minLon, maxLon, largura, margem);
            int y1 = converterY(a.getOrigem().getLatitude(), minLat, maxLat, altura, margem);
            int x2 = converterX(a.getDestino().getLongitude(), minLon, maxLon, largura, margem);
            int y2 = converterY(a.getDestino().getLatitude(), minLat, maxLat, altura, margem);

            Color corInterna = COR_RUA_LIVRE;
            float espessura = 5.0f;

            if (a.getStatus() == StatusVia.CONGESTIONADA) {
                corInterna = COR_RUA_CONGESTIONADA;
                espessura = 7.5f;
            } else if (a.getStatus() == StatusVia.BLOQUEADA) {
                corInterna = COR_RUA_BLOQUEADA;
                espessura = 7.5f;
            }

            g2.setColor(corInterna);
            g2.setStroke(new BasicStroke(espessura, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x1, y1, x2, y2);
        }

        // CAMADA 3: SETAS
        for (Aresta a : grafo.getArestas()) {
            if (a.getStatus() != StatusVia.BLOQUEADA) {
                boolean bidirecional = false;
                for (Aresta volta : grafo.getArestas()) {
                    if (volta.getOrigem().getId() == a.getDestino().getId() && volta.getDestino().getId() == a.getOrigem().getId()) {
                        bidirecional = true;
                        break;
                    }
                }

                if (!bidirecional) {
                    int x1 = converterX(a.getOrigem().getLongitude(), minLon, maxLon, largura, margem);
                    int y1 = converterY(a.getOrigem().getLatitude(), minLat, maxLat, altura, margem);
                    int x2 = converterX(a.getDestino().getLongitude(), minLon, maxLon, largura, margem);
                    int y2 = converterY(a.getDestino().getLatitude(), minLat, maxLat, altura, margem);

                    Color corSeta = (a.getStatus() == StatusVia.CONGESTIONADA) ? new Color(160, 60, 0) : new Color(100, 110, 120);
                    desenharSetaSentido(g2, x1, y1, x2, y2, corSeta);
                }
            }
        }

        // CAMADA 4: ÍCONES DE BLOQUEIO
        for (Aresta a : grafo.getArestas()) {
            if (a.getStatus() == StatusVia.BLOQUEADA) {
                int x1 = converterX(a.getOrigem().getLongitude(), minLon, maxLon, largura, margem);
                int y1 = converterY(a.getOrigem().getLatitude(), minLat, maxLat, altura, margem);
                int x2 = converterX(a.getDestino().getLongitude(), minLon, maxLon, largura, margem);
                int y2 = converterY(a.getDestino().getLatitude(), minLat, maxLat, altura, margem);

                int meioX = (x1 + x2) / 2;
                int meioY = (y1 + y2) / 2;
                g2.setColor(Color.WHITE);
                g2.fillOval(meioX - 8, meioY - 8, 16, 16);
                g2.setColor(COR_RUA_BLOQUEADA);
                g2.setStroke(new BasicStroke(3.0f));
                g2.drawLine(meioX - 5, meioY - 5, meioX + 5, meioY + 5);
                g2.drawLine(meioX - 5, meioY + 5, meioX + 5, meioY - 5);
            }
        }
    }

    private void desenharVertices(Graphics2D g2, double minLon, double maxLon, double minLat, double maxLat, int largura, int altura, int margem) {
        for (Vertice v : grafo.getVertices()) {
            // Ignora pacientes na renderização do mapa (são pontos de ocorrência, não devem poluir visualmente)
            if (v.getTipo() == TipoVertice.PACIENTE) continue;

            int x = converterX(v.getLongitude(), minLon, maxLon, largura, margem);
            int y = converterY(v.getLatitude(), minLat, maxLat, altura, margem);

            if (v instanceof Hospital) {
                desenharPredioHospital(g2, x, y);
            } else if (v.getTipo() == TipoVertice.BASE_SAMU) {
                // Desenha um ícone profissional de base SAMU com estrela da vida
                // Sombra
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval(x - 10, y + 6, 20, 5);
                
                // Círculo base azul escuro
                g2.setColor(new Color(15, 32, 67));
                g2.fillOval(x - 10, y - 10, 20, 20);
                g2.setColor(new Color(30, 60, 110));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(x - 10, y - 10, 20, 20);
                
                // Círculo interno branco
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 6, y - 6, 12, 12);
                
                // Cruz vermelha (símbolo SAMU/emergência)
                g2.setColor(new Color(220, 53, 69));
                g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(x, y - 4, x, y + 4);
                g2.drawLine(x - 4, y, x + 4, y);
                
                // Pequeno detalhe brilhante
                g2.setColor(new Color(255, 255, 255, 80));
                g2.fillOval(x - 3, y - 7, 4, 3);
            } else {
                g2.setColor(new Color(80, 85, 90));
                g2.fillOval(x - 5, y - 5, 10, 10);
                g2.setColor(Color.WHITE);
                g2.fillOval(x - 2, y - 2, 4, 4);
            }
        }
    }

    private void desenharPredioHospital(Graphics2D g2, int x, int y) {
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(x - 11, y + 7, 22, 3, 2, 2);

        g2.setColor(new Color(226, 232, 240));
        g2.fillRect(x - 11, y - 4, 22, 12);
        g2.setColor(new Color(100, 116, 139));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRect(x - 11, y - 4, 22, 12);

        g2.setColor(new Color(248, 250, 252));
        g2.fillRect(x - 5, y - 11, 10, 19);
        g2.setColor(new Color(100, 116, 139));
        g2.drawRect(x - 5, y - 11, 10, 19);

        g2.setColor(new Color(56, 189, 248));
        g2.fillRect(x - 9, y - 1, 3, 3);
        g2.fillRect(x - 9, y + 4, 3, 3);
        g2.fillRect(x + 6, y - 1, 3, 3);
        g2.fillRect(x + 6, y + 4, 3, 3);

        g2.setColor(new Color(2, 132, 199));
        g2.fillRect(x - 3, y + 3, 6, 5);

        g2.setColor(new Color(220, 53, 69));
        g2.fillRect(x - 1, y - 9, 2, 6);
        g2.fillRect(x - 3, y - 7, 6, 2);
    }

    /**
     * Desenha o ícone da Base SAMU na legenda (versão menor para caber no espaço da legenda).
     */
    private void desenharBaseSamuLegenda(Graphics2D g2, int x, int y) {
        // Círculo base azul escuro
        g2.setColor(new Color(15, 32, 67));
        g2.fillOval(x - 7, y - 7, 14, 14);
        g2.setColor(new Color(30, 60, 110));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(x - 7, y - 7, 14, 14);
        
        // Círculo interno branco
        g2.setColor(Color.WHITE);
        g2.fillOval(x - 4, y - 4, 8, 8);
        
        // Cruz vermelha
        g2.setColor(new Color(220, 53, 69));
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x, y - 3, x, y + 3);
        g2.drawLine(x - 3, y, x + 3, y);
    }

    private void desenharAmbulancias(Graphics2D g2, double minLon, double maxLon, double minLat, double maxLat, int largura, int altura, int margem) {
        if (ambulancias == null) return;

        // Pequenos offsets para evitar sobreposição visual quando ambulâncias estão no mesmo local
        int[] offsetsX = {0, 8, -8, 12, -12, 16, -16};
        int idx = 0;

        for (Ambulancia amb : ambulancias) {
            int baseX = converterX(amb.getLongitudeAtual(), minLon, maxLon, largura, margem);
            int baseY = converterY(amb.getLatitudeAtual(), minLat, maxLat, altura, margem);

            // Aplica offset para evitar sobreposição
            int x = baseX - 14 + offsetsX[idx % offsetsX.length];
            int y = baseY - 10 + ((idx / offsetsX.length) * 6);
            idx++;

            String lblAmb = "A" + amb.getId();
            desenharAmbulanciaLateral(g2, x, y, amb.isDisponivel(), lblAmb);
        }
    }

    private void desenharAmbulanciaLateral(Graphics2D g2, int x, int y, boolean disponivel, String lblAmb) {
        Color corStatus = disponivel ? new Color(40, 167, 69) : new Color(253, 126, 20);

        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(x - 11, y + 4, 22, 5);

        g2.setColor(Color.WHITE);
        g2.fillRoundRect(x - 12, y - 6, 24, 12, 4, 4);

        g2.setColor(new Color(220, 53, 69));
        g2.fillRect(x - 12, y, 24, 2);

        g2.setColor(new Color(50, 55, 60));
        g2.fillRect(x - 12, y + 4, 24, 2);

        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(x - 8, y + 2, 6, 6);
        g2.fillOval(x + 3, y + 2, 6, 6);
        g2.setColor(new Color(200, 205, 210));
        g2.fillOval(x - 6, y + 4, 2, 2);
        g2.fillOval(x + 5, y + 4, 2, 2);

        g2.setColor(new Color(30, 45, 60));
        g2.fillRect(x + 4, y - 4, 5, 4);
        g2.fillRect(x - 10, y - 4, 8, 3);

        g2.setColor(new Color(220, 53, 69));
        g2.fillRect(x - 1, y - 4, 2, 3);
        g2.fillRect(x - 2, y - 3, 4, 1);

        g2.setColor(new Color(220, 53, 69));
        g2.fillRect(x + 2, y - 8, 3, 2);
        g2.setColor(new Color(0, 123, 255));
        g2.fillRect(x + 5, y - 8, 3, 2);

        g2.setColor(new Color(140, 145, 150));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(x - 12, y - 6, 24, 12, 4, 4);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        desenharTextoComHalo(g2, lblAmb, x + 14, y + 3, corStatus.darker());
    }

    private void desenharTextos(Graphics2D g2, double minLon, double maxLon, double minLat, double maxLat, int largura, int altura, int margem) {
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));

        for (Vertice v : grafo.getVertices()) {
            if (v.getTipo() == TipoVertice.PACIENTE) {
                continue;
            }

            String texto = v.getNome();
            if (texto == null || texto.trim().isEmpty()) {
                continue;
            }

            int x = converterX(v.getLongitude(), minLon, maxLon, largura, margem);
            int y = converterY(v.getLatitude(), minLat, maxLat, altura, margem);

            int txtX = x + 11;
            int txtY = y + 5;

            Color corFonte = COR_TEXTO;
            if (v instanceof Hospital) {
                corFonte = new Color(180, 20, 30);
                txtY = y - 12;
            } else if (v.getTipo() == TipoVertice.BASE_SAMU) {
                corFonte = new Color(15, 32, 67);
            }

            desenharTextoComHalo(g2, texto, txtX, txtY, corFonte);
        }
    }

    private void desenharTextoComHalo(Graphics2D g2, String texto, int x, int y, Color corPrincipal) {
        g2.setColor(Color.WHITE);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx != 0 || dy != 0) g2.drawString(texto, x + dx, y + dy);
            }
        }
        g2.setColor(corPrincipal);
        g2.drawString(texto, x, y);
    }

    private void desenharLegenda(Graphics2D g2) {
        int lx = 15;
        int ly = 15;
        int lw = 195;
        int lh = 200;

        g2.setColor(new Color(255, 255, 255, 230));
        g2.fillRoundRect(lx, ly, lw, lh, 8, 8);
        g2.setColor(new Color(180, 180, 180));
        g2.drawRoundRect(lx, ly, lw, lh, 8, 8);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        g2.setColor(COR_TEXTO);
        g2.drawString("LEGENDA DO MAPA", lx + 12, ly + 20);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        desenharItemLegenda(g2, lx + 10, ly + 42, COR_RUA_LIVRE, "Via Livre", true);
        desenharItemLegenda(g2, lx + 10, ly + 64, COR_RUA_CONGESTIONADA, "Congestionamento", true);
        desenharItemLegenda(g2, lx + 10, ly + 86, COR_RUA_BLOQUEADA, "Via Bloqueada", true);
        
        desenharSetaSentido(g2, lx + 6, ly + 106, lx + 22, ly + 106, new Color(100, 110, 120));
        g2.setColor(COR_TEXTO);
        g2.drawString("Via de Mão Única", lx + 28, ly + 110);

        // Ícone Rota
        g2.setColor(COR_ROTA);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(lx + 6, ly + 128, lx + 22, ly + 128);
        g2.setColor(COR_TEXTO);
        g2.drawString("Rota da Ocorrência", lx + 28, ly + 132);

        desenharPredioHospital(g2, lx + 16, ly + 150);
        g2.setColor(COR_TEXTO);
        g2.drawString("Hospital / UPA", lx + 34, ly + 154);

        // Ícone Base SAMU
        desenharBaseSamuLegenda(g2, lx + 16, ly + 172);
        g2.setColor(COR_TEXTO);
        g2.drawString("Base SAMU", lx + 34, ly + 176);
    }

    private void desenharItemLegenda(Graphics2D g2, int x, int y, Color cor, String texto, boolean isLinha) {
        if (isLinha) {
            g2.setColor(COR_RUA_BORDAS);
            g2.setStroke(new BasicStroke(4));
            g2.drawLine(x, y - 3, x + 14, y - 3);
            g2.setColor(cor);
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(x, y - 3, x + 14, y - 3);
        }
        g2.setColor(COR_TEXTO);
        g2.drawString(texto, x + 22, y + 1);
    }

    private int converterX(double longitude, double min, double max, int largura, int margem) {
        int centroX = getWidth() / 2;
        int baseX = (max == min) ? margem + largura / 2 : margem + (int) ((longitude - min) / (max - min) * largura);
        return (int) (centroX + (baseX - centroX) * fatorZoom + offsetX);
    }

    private int converterY(double latitude, double min, double max, int altura, int margem) {
        int centroY = getHeight() / 2;
        int baseY = (max == min) ? margem + altura / 2 : margem + altura - (int) ((latitude - min) / (max - min) * altura);
        return (int) (centroY + (baseY - centroY) * fatorZoom + offsetY);
    }

    private void desenharSetaSentido(Graphics2D g2, int x1, int y1, int x2, int y2, Color corSeta) {
        double t = 0.50;
        
        int pontoX = (int) (x1 + (x2 - x1) * t);
        int pontoY = (int) (y1 + (y2 - y1) * t);

        double angulo = Math.atan2(y2 - y1, x2 - x1);

        double tamanho = 5.5; 
        double abertura = Math.PI / 4.5;

        int xPonta = pontoX;
        int yPonta = pontoY;
        int xEsq = (int) (pontoX - tamanho * Math.cos(angulo - abertura));
        int yEsq = (int) (pontoY - tamanho * Math.sin(angulo - abertura));
        int xDir = (int) (pontoX - tamanho * Math.cos(angulo + abertura));
        int yDir = (int) (pontoY - tamanho * Math.sin(angulo + abertura));

        g2.setColor(corSeta);
        g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(xEsq, yEsq, xPonta, yPonta);
        g2.drawLine(xDir, yDir, xPonta, yPonta);
    }

    /**
     * Retorna a ambulância atualmente em atendimento (para recalcular rota).
     */
    public Ambulancia getAmbulanciaEmAtendimento() {
        if (ambulancias == null) return null;
        for (Ambulancia amb : ambulancias) {
            if (amb.estaEmAtendimento()) return amb;
        }
        return null;
    }

    /**
     * Retorna a ambulância mais recentemente em atendimento (pela posição que não está na base).
     */
    public Ambulancia getUltimaAmbulanciaDespachada() {
        if (ambulancias == null) return null;
        Ambulancia ultima = null;
        for (Ambulancia amb : ambulancias) {
            if (!amb.isDisponivel()) {
                ultima = amb;
            }
        }
        return ultima;
    }
}