package grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Orquestrador central do sistema de emergência SAMU.
 * Coordena o fluxo completo de atendimento:
 * 1. Registrar ocorrência
 * 2. Localizar ambulância mais próxima disponível
 * 3. Calcular rota ambulância → paciente (Dijkstra)
 * 4. Selecionar hospital disponível mais próximo (A*)
 * 5. Calcular rota paciente → hospital (Dijkstra)
 * 6. Monitorar e recalcular rotas em caso de bloqueio
 * 7. Análise de cobertura da rede (Componentes Conexos)
 */
public class SistemaEmergencia {

    private final GrafoCidade grafo;
    private final List<Ambulancia> ambulancias;
    private final List<Hospital> hospitais;
    private final List<Paciente> pacientes;

    /**
     * Construtor do sistema de emergência.
     *
     * @param grafo o grafo da cidade
     */
    public SistemaEmergencia() {
        this.grafo = new GrafoCidade();
        this.ambulancias = new ArrayList<>();
        this.hospitais = new ArrayList<>();
        this.pacientes = new ArrayList<>();
        
    }

    // ==================== Cadastros (T-13 a T-16) ====================

    /**
     * Cadastra um hospital na rede (RF01).
     *
     * @param hospital hospital a ser cadastrado
     * @return true se cadastrado com sucesso
     */
    public boolean cadastrarHospital(Hospital hospital) {
        if (hospital == null) return false;
        boolean adicionado = grafo.addVertice(hospital);
        if (adicionado) {
            hospitais.add(hospital);
        }
        return adicionado;
    }

    /**
     * Cadastra uma base SAMU no grafo (RF02).
     *
     * @param base vértice do tipo BASE_SAMU
     * @return true se cadastrado com sucesso
     */
    public boolean cadastrarBaseSamu(Vertice base) {
        if (base == null || base.getTipo() != TipoVertice.BASE_SAMU) return false;
        return grafo.addVertice(base);
    }

    /**
     * Cadastra um bairro ou cruzamento no grafo (RF03).
     *
     * @param vertice vértice do tipo BAIRRO ou CRUZAMENTO
     * @return true se cadastrado com sucesso
     */
    public boolean cadastrarVertice(Vertice vertice) {
        if (vertice == null) return false;
        return grafo.addVertice(vertice);
    }

    /**
     * Cadastra uma via (aresta) no grafo (RF04).
     *
     * @param aresta aresta a ser cadastrada
     * @return true se cadastrada com sucesso
     */
    public boolean cadastrarVia(Aresta aresta) {
        if (aresta == null) return false;
        return grafo.addAresta(aresta);
    }

    /**
     * Cadastra uma via bidirecional no grafo (RF04).
     * Cria duas arestas: origem→destino e destino→origem com o mesmo peso e status.
     *
     * @param origem  vértice de origem
     * @param destino vértice de destino
     * @param peso    tempo de deslocamento em minutos
     * @param status  status inicial da via
     * @return true se ambas as arestas foram adicionadas com sucesso
     */
    public boolean cadastrarViaBidirecional(Vertice origem, Vertice destino, double peso, StatusVia status) {
        if (origem == null || destino == null) return false;
        Aresta ida = new Aresta(origem, destino, peso, status);
        Aresta volta = new Aresta(destino, origem, peso, status);
        boolean adicionouIda = grafo.addAresta(ida);
        boolean adicionouVolta = grafo.addAresta(volta);
        return adicionouIda && adicionouVolta;
    }

    /**
     * Cadastra uma via bidirecional livre no grafo (RF04).
     *
     * @param origem  vértice de origem
     * @param destino vértice de destino
     * @param peso    tempo de deslocamento em minutos
     * @return true se ambas as arestas foram adicionadas com sucesso
     */
    public boolean cadastrarViaBidirecional(Vertice origem, Vertice destino, double peso) {
        return cadastrarViaBidirecional(origem, destino, peso, StatusVia.LIVRE);
    }

    /**
     * Cadastra uma ambulância no sistema.
     *
     * @param ambulancia ambulância a ser cadastrada
     * @return true se cadastrada com sucesso
     */
    public boolean cadastrarAmbulancia(Ambulancia ambulancia) {
        if (ambulancia == null) return false;
        ambulancias.add(ambulancia);
        return true;
    }

    // ==================== Fluxo de Emergência ====================

    /**
     * Conecta um paciente ao vértice mais próximo do grafo (RN05, T-09).
     * Cria arestas bidirecionais entre o paciente e o vértice mais próximo
     * (ignorando outros pacientes como candidato), com peso estimado pela
     * mesma heurística de velocidade média usada no A* (40 km/h).
     * Necessário para que Dijkstra/A* consigam alcançar o paciente.
     *
     * @param paciente paciente a ser conectado
     * @return true se a conexão foi criada com sucesso
     */
    public boolean conectarPacienteAoVerticeMaisProximo(Paciente paciente) {
        if (paciente == null) return false;

        Vertice maisProximo = null;
        double menorDistancia = Double.POSITIVE_INFINITY;

        for (Vertice v : grafo.getVertices()) {
            if (v.equals(paciente) || v instanceof Paciente) continue;
            double distancia = paciente.calcularDistancia(v);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                maisProximo = v;
            }
        }

        if (maisProximo == null) return false;

        double tempoEstimado = (menorDistancia / 40.0) * 60.0; // minutos, 40 km/h
        boolean ida = grafo.addAresta(new Aresta(maisProximo, paciente, tempoEstimado, StatusVia.LIVRE));
        boolean volta = grafo.addAresta(new Aresta(paciente, maisProximo, tempoEstimado, StatusVia.LIVRE));
        return ida && volta;
    }

    /**
     * Encapsula o resultado completo do atendimento a uma nova ocorrência (RN05):
     * ambulância designada, rota até o paciente e hospital de destino selecionado.
     */
    public static class AtendimentoResultado {
        private final Paciente paciente;
        private final Ambulancia ambulancia;
        private final Dijkstra.Resultado rotaAmbulancia;
        private final AEstrela.Resultado rotaHospital;
        private final String mensagem;

        public AtendimentoResultado(Paciente paciente, Ambulancia ambulancia,
                                     Dijkstra.Resultado rotaAmbulancia,
                                     AEstrela.Resultado rotaHospital, String mensagem) {
            this.paciente = paciente;
            this.ambulancia = ambulancia;
            this.rotaAmbulancia = rotaAmbulancia;
            this.rotaHospital = rotaHospital;
            this.mensagem = mensagem;
        }

        public Paciente getPaciente() { return paciente; }
        public Ambulancia getAmbulancia() { return ambulancia; }
        public Dijkstra.Resultado getRotaAmbulancia() { return rotaAmbulancia; }
        public AEstrela.Resultado getRotaHospital() { return rotaHospital; }
        public String getMensagem() { return mensagem; }

        public boolean isSucesso() {
            return ambulancia != null && rotaAmbulancia != null && rotaAmbulancia.temCaminho();
        }
    }

    /**
     * Orquestra a análise de rota completa para uma nova ocorrência (RN05):
     * localizar ambulância → calcular rota até o paciente → selecionar hospital.
     * Chamado automaticamente por registrarOcorrencia(), garantindo a regra de negócio
     * independentemente de quem invoca o sistema.
     *
     * @param paciente paciente recém-registrado
     * @return resultado consolidado do atendimento
     */
    public AtendimentoResultado atenderNovaOcorrencia(Paciente paciente) {
        Dijkstra.Resultado semRota = new Dijkstra.Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
        AEstrela.Resultado semHospital = new AEstrela.Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);

        if (paciente == null) {
            return new AtendimentoResultado(null, null, semRota, semHospital, "Paciente inválido.");
        }

        Ambulancia ambulancia = localizarAmbulanciaProxima(paciente);
        if (ambulancia == null) {
            return new AtendimentoResultado(paciente, null, semRota, semHospital,
                    "Nenhuma ambulância disponível no momento.");
        }

        Dijkstra.Resultado rotaAmbulancia = calcularRotaAmbulanciaParaPaciente(ambulancia, paciente);
        if (!rotaAmbulancia.temCaminho()) {
            return new AtendimentoResultado(paciente, ambulancia, rotaAmbulancia, semHospital,
                    "VIAS BLOQUEADAS: Nenhuma alternativa encontrada para o destino.");
        }

        AEstrela.Resultado rotaHospital = selecionarHospitalDestino(paciente);
        String mensagem = rotaHospital.temCaminho()
                ? "Ocorrência atendida: ambulância e hospital designados."
                : "VIAS BLOQUEADAS: Nenhuma alternativa encontrada para o destino.";

        return new AtendimentoResultado(paciente, ambulancia, rotaAmbulancia, rotaHospital, mensagem);
    }

    /**
     * Etapa 1 — Registra uma ocorrência de emergência (RF05, RN05).
     * Adiciona o paciente ao grafo, conecta-o automaticamente ao vértice mais próximo,
     * e dispara a análise de rota completa (ambulância + hospital) — a regra de negócio
     * RN05 deixa de depender do código-cliente e passa a ser garantida aqui.
     *
     * @param paciente paciente com localização e nível de urgência
     * @return resultado do atendimento, ou null se o paciente não pôde ser registrado
     */
    public AtendimentoResultado registrarOcorrencia(Paciente paciente) {
        if (paciente == null) return null;

        boolean adicionado = grafo.addVertice(paciente);
        if (!adicionado) return null;

        pacientes.add(paciente);
        conectarPacienteAoVerticeMaisProximo(paciente);

        return atenderNovaOcorrencia(paciente);
    }

    /**
     * Localiza a ambulância disponível mais próxima do paciente (RF06).
     * Aplica Dijkstra de cada ambulância disponível até o paciente e retorna a mais próxima.
     *
     * @param paciente paciente que precisa de atendimento
     * @return ambulância mais próxima disponível, ou null se nenhuma disponível
     */
    public Ambulancia localizarAmbulanciaProxima(Paciente paciente) {
        if (paciente == null || ambulancias.isEmpty()) return null;

        Ambulancia maisProxima = null;
        double menorCusto = Double.POSITIVE_INFINITY;

        for (Ambulancia ambulancia : ambulancias) {
            if (!ambulancia.isDisponivel()) continue;

            Dijkstra.Resultado resultado = Dijkstra.encontrarMenorCaminho(
                    grafo, ambulancia.getLocalizacaoAtual(), paciente);

            if (resultado.temCaminho() && resultado.getCustoTotal() < menorCusto) {
                menorCusto = resultado.getCustoTotal();
                maisProxima = ambulancia;
            }
        }

        return maisProxima;
    }

    /**
     * Calcula a rota da ambulância até o paciente usando Dijkstra (RF07).
     *
     * @param ambulancia ambulância despachada
     * @param paciente   paciente a ser atendido
     * @return resultado do Dijkstra com caminho e custo
     */
    public Dijkstra.Resultado calcularRotaAmbulanciaParaPaciente(Ambulancia ambulancia, Paciente paciente) {
        if (ambulancia == null || paciente == null) {
            return new Dijkstra.Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }
        return Dijkstra.encontrarMenorCaminho(grafo, ambulancia.getLocalizacaoAtual(), paciente);
    }

    /**
     * Seleciona o hospital disponível mais próximo do paciente usando A* (RF08, RN02).
     *
     * @param paciente paciente a ser transportado
     * @return resultado do A* com caminho até o hospital mais próximo disponível
     */
    public AEstrela.Resultado selecionarHospitalDestino(Paciente paciente) {
        if (paciente == null) {
            return new AEstrela.Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }
        return AEstrela.encontrarHospitalMaisProximo(grafo, paciente, hospitais);
    }

    /**
     * Estima o tempo de chegada (ETA) com base no custo do caminho (RF11).
     * O custo já representa minutos — este método formata para exibição.
     *
     * @param custoEmMinutos custo total do caminho em minutos
     * @return string formatada com o ETA (ex: "8 min" ou "1h 12min")
     */
    public String estimarTempoChegada(double custoEmMinutos) {
        if (custoEmMinutos == Double.POSITIVE_INFINITY || custoEmMinutos < 0) {
            return "Indisponível";
        }
        int minutos = (int) Math.round(custoEmMinutos);
        if (minutos < 60) {
            return minutos + " min";
        }
        int horas = minutos / 60;
        int mins = minutos % 60;
        return horas + "h " + mins + "min";
    }

    /**
     * Recalcula a rota em caso de bloqueio de via (RF10, RN03).
     *
     * @param aresta       via bloqueada
     * @param rotaAtual    rota em uso no momento do bloqueio
     * @param posicaoAtual posição atual da ambulância
     * @param destino      destino atual da ambulância
     * @return resultado do recálculo de rota
     */
    public RecalculoRota.Resultado recalcularRota(Aresta aresta, List<Vertice> rotaAtual,
                                                   Vertice posicaoAtual, Vertice destino) {
        return RecalculoRota.bloquearViaERecalcular(grafo, aresta, rotaAtual, destino, posicaoAtual);
    }

    /**
     * Analisa a cobertura da rede (RF12).
     * Identifica regiões sem acesso hospitalar usando Componentes Conexos.
     *
     * @return resultado da análise de componentes conexos
     */
    public ComponentesConexos.Resultado analisarCobertura() {
        return ComponentesConexos.identificarComponentes(grafo);
    }

    /**
     * Despacha uma ambulância para atender um paciente.
     * Marca a ambulância como EM_ATENDIMENTO (RN01).
     *
     * @param ambulancia ambulância a ser despachada
     * @return true se despachada com sucesso
     */
    public boolean despacharAmbulancia(Ambulancia ambulancia) {
        if (ambulancia == null) return false;
        return ambulancia.iniciarAtendimento();
    }

    /**
     * Finaliza o atendimento de uma ambulância, marcando-a como DISPONIVEL.
     *
     * @param ambulancia ambulância que concluiu o atendimento
     * @param novaLocalizacao localização final da ambulância (hospital de destino)
     * @return true se finalizado com sucesso
     */
    public boolean finalizarAtendimento(Ambulancia ambulancia, Vertice novaLocalizacao) {
        if (ambulancia == null) return false;
        ambulancia.setLocalizacaoAtual(novaLocalizacao);
        return ambulancia.finalizarAtendimento();
    }

    // ==================== Getters ====================

    public GrafoCidade getGrafo() {
        return grafo;
    }

    public List<Ambulancia> getAmbulancias() {
        return Collections.unmodifiableList(ambulancias);
    }

    public List<Hospital> getHospitais() {
        return Collections.unmodifiableList(hospitais);
    }

    public List<Paciente> getPacientes() {
        return Collections.unmodifiableList(pacientes);
    }

    /**
     * Retorna apenas as ambulâncias disponíveis.
     *
     * @return lista de ambulâncias disponíveis
     */
    public List<Ambulancia> getAmbulanciasDisponiveis() {
        List<Ambulancia> disponiveis = new ArrayList<>();
        for (Ambulancia a : ambulancias) {
            if (a.isDisponivel()) disponiveis.add(a);
        }
        return disponiveis;
    }

    /**
     * Retorna apenas os hospitais com vagas disponíveis (RN02).
     *
     * @return lista de hospitais disponíveis
     */
    public List<Hospital> getHospitaisDisponiveis() {
        List<Hospital> disponiveis = new ArrayList<>();
        for (Hospital h : hospitais) {
            if (h.isDisponivel()) disponiveis.add(h);
        }
        return disponiveis;
    }

    /**
     * Atualiza o status de uma via no grafo (RF09).
     *
     * @param aresta    aresta a ser atualizada
     * @param novoStatus novo status da via
     * @return resultado da operação
     */
    public GerenciadorVias.Resultado atualizarStatusVia(Aresta aresta, StatusVia novoStatus) {
        switch (novoStatus) {
            case BLOQUEADA:
                return GerenciadorVias.bloquearVia(grafo, aresta);
            case CONGESTIONADA:
                return GerenciadorVias.congestionarVia(grafo, aresta);
            case LIVRE:
                return GerenciadorVias.liberarVia(grafo, aresta);
            default:
                return null;
        }
    }
}