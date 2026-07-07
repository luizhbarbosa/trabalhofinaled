package grafo;

import java.util.*;

/**
 * Implementação do algoritmo A* (A Estrela) para encontrar o caminho mais
 * eficiente entre dois vértices no grafo da cidade, usando heurística baseada
 * em distância euclidiana.
 *
 * Usado para localizar o hospital disponível mais próximo do paciente (RF08, T-09).
 * Combina o custo real do caminho (g) com uma estimativa heurística (h) para
 * guiar a busca de forma mais eficiente que o Dijkstra puro.
 */
public class AEstrela {

    /**
     * Classe que encapsula o resultado da busca A*.
     */
    public static class Resultado {
        private final List<Vertice> caminho;
        private final double custoTotal;

        /**
         * Construtor do resultado.
         *
         * @param caminho    lista de vértices que compõem o caminho (vazio se não houver rota)
         * @param custoTotal custo total do caminho (tempo em minutos, ou infinito se não houver rota)
         */
        public Resultado(List<Vertice> caminho, double custoTotal) {
            this.caminho = caminho;
            this.custoTotal = custoTotal;
        }

        /**
         * Obtém o caminho encontrado (lista de vértices da origem ao destino).
         *
         * @return lista de vértices do caminho
         */
        public List<Vertice> getCaminho() {
            return caminho;
        }

        /**
         * Obtém o custo total do caminho.
         *
         * @return custo total em minutos, ou Double.POSITIVE_INFINITY se não houver rota
         */
        public double getCustoTotal() {
            return custoTotal;
        }

        /**
         * Verifica se foi encontrado um caminho válido.
         *
         * @return true se existe rota entre origem e destino
         */
        public boolean temCaminho() {
            return caminho != null && !caminho.isEmpty() && custoTotal != Double.POSITIVE_INFINITY;
        }
    }

    /**
     * Encontra o menor caminho entre origem e destino usando o algoritmo A*.
     * A heurística utilizada é a distância euclidiana entre vértices
     * (em km, convertida para minutos assumindo velocidade média de 40 km/h).
     * <p>
     * Usa a lista de adjacência do grafo (grafo.getArestasSaida) em vez de percorrer
     * todas as arestas a cada vértice processado, garantindo complexidade O(E log V)
     * em vez de O(V×E) (RNF04).
     *
     * @param grafo   o grafo da cidade contendo vértices e arestas
     * @param origem  vértice de origem do caminho
     * @param destino vértice de destino do caminho
     * @return Resultado contendo o caminho e o custo total, ou caminho vazio se não existir rota
     */
    public static Resultado encontrarMenorCaminho(GrafoCidade grafo, Vertice origem, Vertice destino) {
        if (grafo == null || origem == null || destino == null) {
            return new Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        // Caso trivial: origem igual ao destino
        if (origem.equals(destino)) {
            return new Resultado(Collections.singletonList(origem), 0.0);
        }

        // g(n): custo real acumulado da origem até o vértice n
        Map<Vertice, Double> gCusto = new HashMap<>();
        // Mapa de predecessores para reconstrução do caminho
        Map<Vertice, Vertice> predecessores = new HashMap<>();

        // Fila de prioridade ordenada pelo f(n) = g(n) + h(n)
        PriorityQueue<Vertice> abertos = new PriorityQueue<>(
                Comparator.comparingDouble(v -> gCusto.getOrDefault(v, Double.POSITIVE_INFINITY) + heuristica(v, destino))
        );

        // Conjunto de vértices já processados
        Set<Vertice> fechados = new HashSet<>();

        // Inicializa todos os custos como infinito
        for (Vertice v : grafo.getVertices()) {
            gCusto.put(v, Double.POSITIVE_INFINITY);
        }

        gCusto.put(origem, 0.0);
        abertos.add(origem);

        while (!abertos.isEmpty()) {
            Vertice atual = abertos.poll();

            // Se chegamos ao destino, reconstrói e retorna o caminho
            if (atual.equals(destino)) {
                return reconstruirCaminho(origem, destino, predecessores, gCusto.get(destino));
            }

            // Marca como processado
            fechados.add(atual);

            // Explora apenas as arestas que partem do vértice atual (lista de adjacência)
            for (Aresta aresta : grafo.getArestasSaida(atual)) {
                double pesoEfetivo = aresta.getPesoEfetivo();

                // Ignora vias bloqueadas
                if (pesoEfetivo == Double.POSITIVE_INFINITY) continue;

                Vertice vizinho = aresta.getDestino();

                // Ignora vértices já processados
                if (fechados.contains(vizinho)) continue;

                double novoG = gCusto.get(atual) + pesoEfetivo;

                if (novoG < gCusto.getOrDefault(vizinho, Double.POSITIVE_INFINITY)) {
                    gCusto.put(vizinho, novoG);
                    predecessores.put(vizinho, atual);

                    // Remove e re-insere para atualizar a prioridade
                    abertos.remove(vizinho);
                    abertos.add(vizinho);
                }
            }
        }

        // Destino não alcançável
        return new Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
    }

    /**
     * Encontra o hospital disponível mais próximo de um vértice de origem usando A*.
     * Hospitais lotados são ignorados (RN02).
     *
     * @param grafo    o grafo da cidade
     * @param origem   vértice de origem (ex: localização do paciente)
     *
      */
     public static Resultado encontrarHospitalMaisProximo(
        GrafoCidade grafo,
        Paciente paciente,
        List<Hospital> hospitais) {

    if (grafo == null || paciente == null || hospitais == null || hospitais.isEmpty()) {
        return new Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
    }

    Resultado melhorResultado =
            new Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);

    for (Hospital hospital : hospitais) {

        if (!hospital.isDisponivel()) {
            continue;
        }

        Resultado resultado = encontrarMenorCaminho(grafo, paciente, hospital);

        if (resultado.temCaminho()
                && resultado.getCustoTotal() < melhorResultado.getCustoTotal()) {
            melhorResultado = resultado;
        }
    }

    return melhorResultado;
    } // <--- Adicione esta chave de fechamento para fechar o método encontrarHospitalMaisProximo

    private static Resultado reconstruirCaminho(
        Vertice origem,
        Vertice destino,
        Map<Vertice, Vertice> predecessores,
        double custoTotal) {

    List<Vertice> caminho = new ArrayList<>();

    Vertice atual = destino;

    while (atual != null) {
        caminho.add(atual);

        if (atual.equals(origem)) {
            break;
        }

        atual = predecessores.get(atual);
    }

    Collections.reverse(caminho);

    if (caminho.isEmpty() || !caminho.get(0).equals(origem)) {
        return new Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
    }

    return new Resultado(caminho, custoTotal);
}
/**
 * Heurística do algoritmo A*.
 * Estima o custo restante usando a distância euclidiana entre os vértices.
 * A distância é convertida para minutos assumindo velocidade média de 40 km/h.
 *
 * @param atual vértice atual
 * @param destino vértice destino
 * @return estimativa de custo até o destino
 */
private static double heuristica(Vertice atual, Vertice destino) {
    if (atual == null || destino == null) {
        return Double.POSITIVE_INFINITY;
    }

    double distanciaKm = atual.calcularDistancia(destino);

    // velocidade média = 40 km/h
    return (distanciaKm / 40.0) * 60.0;
}
}