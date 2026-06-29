package grafo;

import java.util.Collections;
import java.util.List;

/**
 * Serviço responsável por monitorar bloqueios de via e recalcular rotas
 * automaticamente usando Dijkstra (RF10, RN03).
 *
 * Quando uma via é bloqueada durante um atendimento, este serviço detecta
 * se a rota atual foi afetada e calcula um novo caminho alternativo.
 */
public class RecalculoRota {

    /**
     * Classe que encapsula o resultado de um recálculo de rota.
     */
    public static class Resultado {
        private final List<Vertice> caminho;
        private final double custoTotal;
        private final boolean rotaRecalculada;
        private final String mensagem;

        public Resultado(List<Vertice> caminho, double custoTotal, boolean rotaRecalculada, String mensagem) {
            this.caminho = caminho;
            this.custoTotal = custoTotal;
            this.rotaRecalculada = rotaRecalculada;
            this.mensagem = mensagem;
        }

        public List<Vertice> getCaminho() {
            return caminho;
        }

        public double getCustoTotal() {
            return custoTotal;
        }

        /**
         * Indica se a rota foi recalculada (houve bloqueio detectado).
         */
        public boolean isRotaRecalculada() {
            return rotaRecalculada;
        }

        public String getMensagem() {
            return mensagem;
        }

        public boolean temCaminho() {
            return caminho != null && !caminho.isEmpty() && custoTotal != Double.POSITIVE_INFINITY;
        }

        @Override
        public String toString() {
            return "RecalculoRota.Resultado{" +
                    "rotaRecalculada=" + rotaRecalculada +
                    ", custoTotal=" + custoTotal +
                    ", mensagem='" + mensagem + '\'' +
                    ", caminho=" + caminho +
                    '}';
        }
    }

    /**
     * Bloqueia uma via e verifica se a rota atual foi afetada.
     * Se sim, recalcula automaticamente com Dijkstra (RN03).
     *
     * @param grafo        o grafo da cidade
     * @param aresta       a aresta (via) a ser bloqueada
     * @param rotaAtual    rota em uso no momento do bloqueio
     * @param destino      vértice de destino da rota atual
     * @param posicaoAtual posição atual da ambulância na rota
     * @return Resultado com nova rota (se necessário) ou rota original confirmada
     */
    public static Resultado bloquearViaERecalcular(GrafoCidade grafo, Aresta aresta,
                                                    List<Vertice> rotaAtual, Vertice destino,
                                                    Vertice posicaoAtual) {
        if (grafo == null || aresta == null) {
            return new Resultado(rotaAtual, Double.POSITIVE_INFINITY, false, "Parâmetros inválidos.");
        }

        // Bloqueia a via
        aresta.setStatus(StatusVia.BLOQUEADA);

        // Verifica se a via bloqueada afeta a rota atual
        if (!rotaContemAresta(rotaAtual, aresta)) {
            return new Resultado(
                    rotaAtual,
                    calcularCustoRota(rotaAtual, grafo),
                    false,
                    "Via bloqueada não afeta a rota atual."
            );
        }

        // Rota afetada: recalcula a partir da posição atual
        Dijkstra.Resultado novaRota = Dijkstra.encontrarMenorCaminho(grafo, posicaoAtual, destino);

        if (!novaRota.temCaminho()) {
            return new Resultado(
                    Collections.emptyList(),
                    Double.POSITIVE_INFINITY,
                    true,
                    "Via bloqueada. Nenhuma rota alternativa encontrada para o destino."
            );
        }

        return new Resultado(
                novaRota.getCaminho(),
                novaRota.getCustoTotal(),
                true,
                "Via bloqueada. Rota recalculada automaticamente a partir de '"
                        + posicaoAtual.getNome() + "'."
        );
    }

    /**
     * Reativa uma via bloqueada ou congestionada e opcionalmente recalcula
     * a rota para verificar se o novo caminho é mais eficiente.
     *
     * @param grafo        o grafo da cidade
     * @param aresta       a aresta a ser reaberta
     * @param novoStatus   novo status da via (LIVRE ou CONGESTIONADA)
     * @param rotaAtual    rota em uso no momento da reabertura
     * @param posicaoAtual posição atual da ambulância
     * @param destino      vértice de destino
     * @return Resultado com rota otimizada se houver caminho melhor, ou rota atual mantida
     */
    public static Resultado reabrirViaEOtimizar(GrafoCidade grafo, Aresta aresta,
                                                 StatusVia novoStatus, List<Vertice> rotaAtual,
                                                 Vertice posicaoAtual, Vertice destino) {
        if (grafo == null || aresta == null || novoStatus == StatusVia.BLOQUEADA) {
            return new Resultado(rotaAtual, calcularCustoRota(rotaAtual, grafo), false,
                    "Parâmetros inválidos para reabertura.");
        }

        // Reativa a via com o novo status
        aresta.setStatus(novoStatus);

        // Recalcula para verificar se há caminho mais eficiente
        Dijkstra.Resultado novaRota = Dijkstra.encontrarMenorCaminho(grafo, posicaoAtual, destino);
        double custoAtual = calcularCustoRota(rotaAtual, grafo);

        if (!novaRota.temCaminho()) {
            return new Resultado(rotaAtual, custoAtual, false, "Via reaberta. Rota atual mantida.");
        }

        if (novaRota.getCustoTotal() < custoAtual) {
            return new Resultado(
                    novaRota.getCaminho(),
                    novaRota.getCustoTotal(),
                    true,
                    "Via reaberta. Rota otimizada encontrada."
            );
        }

        return new Resultado(rotaAtual, custoAtual, false, "Via reaberta. Rota atual já é a mais eficiente.");
    }

    /**
     * Verifica se uma rota contém a aresta bloqueada.
     *
     * @param rota   lista de vértices da rota
     * @param aresta aresta a verificar
     * @return true se a aresta faz parte da rota
     */
    private static boolean rotaContemAresta(List<Vertice> rota, Aresta aresta) {
        if (rota == null || rota.size() < 2) return false;

        for (int i = 0; i < rota.size() - 1; i++) {
            Vertice origem = rota.get(i);
            Vertice destino = rota.get(i + 1);

            if (origem.equals(aresta.getOrigem()) && destino.equals(aresta.getDestino())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcula o custo total de uma rota com base nos pesos efetivos das arestas no grafo.
     *
     * @param rota  lista de vértices da rota
     * @param grafo o grafo da cidade
     * @return custo total da rota em minutos
     */
    private static double calcularCustoRota(List<Vertice> rota, GrafoCidade grafo) {
        if (rota == null || rota.size() < 2 || grafo == null) return Double.POSITIVE_INFINITY;

        double custo = 0.0;
        for (int i = 0; i < rota.size() - 1; i++) {
            Vertice origem = rota.get(i);
            Vertice destino = rota.get(i + 1);

            double pesoAresta = grafo.getArestas().stream()
                    .filter(a -> a.getOrigem().equals(origem) && a.getDestino().equals(destino))
                    .mapToDouble(Aresta::getPesoEfetivo)
                    .findFirst()
                    .orElse(Double.POSITIVE_INFINITY);

            custo += pesoAresta;
        }
        return custo;
    }
}