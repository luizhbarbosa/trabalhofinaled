package grafo;

import java.util.*;

/**
 * Implementação do algoritmo de Dijkstra para encontrar o menor caminho
 * (menor custo/tempo) entre dois vértices no grafo da cidade.
 *
 * Considera o peso efetivo das arestas, levando em conta bloqueios e congestionamentos.
 * Usado para calcular rotas de ambulância (RF07) e estimar tempo de chegada (RF11).
 */
public class Dijkstra {

    /**
     * Classe que encapsula o resultado da busca pelo menor caminho.
     */
    public static class Resultado {
        private final List<Vertice> caminho;
        private final double custoTotal;

        /**
         * Construtor do resultado.
         *
         * @param caminho   lista de vértices que compõem o caminho (vazio se não houver rota)
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
         * Obtém o custo total do caminho (soma dos pesos efetivos das arestas).
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
     * Encontra o menor caminho entre dois vértices usando o algoritmo de Dijkstra.
     * <p>
     * O algoritmo considera o peso efetivo das arestas, que pode ser infinito para
     * vias bloqueadas ou multiplicado para vias congestionadas.
     * <p>
     * Usa a lista de adjacência do grafo (grafo.getArestasSaida) em vez de percorrer
     * todas as arestas a cada vértice processado, garantindo complexidade O(E log V)
     * em vez de O(V×E) (RNF04).
     *
     * @param grafo    o grafo da cidade contendo vértices e arestas
     * @param origem   vértice de origem do caminho
     * @param destino  vértice de destino do caminho
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

        // Mapa de distâncias: vértice -> menor custo conhecido a partir da origem
        Map<Vertice, Double> distancias = new HashMap<>();
        // Mapa de predecessores: vértice -> vértice anterior no menor caminho
        Map<Vertice, Vertice> predecessores = new HashMap<>();
        // Fila de prioridade (min-heap) ordenada pela distância
        // Usamos AbstractMap.SimpleEntry para armazenar (vértice, distância) e detectar entradas desatualizadas
        PriorityQueue<Map.Entry<Vertice, Double>> filaPrioridade = new PriorityQueue<>(
                Comparator.comparingDouble(Map.Entry::getValue)
        );

        // Inicializa todas as distâncias como infinito
        for (Vertice v : grafo.getVertices()) {
            distancias.put(v, Double.POSITIVE_INFINITY);
        }

        // Define a distância da origem como 0 e adiciona à fila
        distancias.put(origem, 0.0);
        filaPrioridade.add(new AbstractMap.SimpleEntry<>(origem, 0.0));

        while (!filaPrioridade.isEmpty()) {
            Map.Entry<Vertice, Double> entry = filaPrioridade.poll();
            Vertice atual = entry.getKey();
            double distanciaAtual = entry.getValue();

            // Se esta entrada na fila tem distância maior que a já registrada, é uma entrada desatualizada
            if (distanciaAtual > distancias.get(atual)) {
                continue;
            }

            // Se chegamos ao destino com a menor distância, podemos parar
            if (atual.equals(destino)) {
                break;
            }

            // Explora apenas as arestas que partem do vértice atual (lista de adjacência)
            for (Aresta aresta : grafo.getArestasSaida(atual)) {
                Vertice vizinho = aresta.getDestino();
                double pesoEfetivo = aresta.getPesoEfetivo();

                // Ignora vias bloqueadas (peso infinito)
                if (pesoEfetivo == Double.POSITIVE_INFINITY) {
                    continue;
                }

                double novaDistancia = distancias.get(atual) + pesoEfetivo;

                // Se encontramos um caminho mais curto para o vizinho, atualizamos
                if (novaDistancia < distancias.get(vizinho)) {
                    distancias.put(vizinho, novaDistancia);
                    predecessores.put(vizinho, atual);
                    filaPrioridade.add(new AbstractMap.SimpleEntry<>(vizinho, novaDistancia));
                }
            }
        }

        // Verifica se o destino é alcançável
        if (distancias.get(destino) == Double.POSITIVE_INFINITY) {
            return new Resultado(Collections.emptyList(), Double.POSITIVE_INFINITY);
        }

        // Reconstrói o caminho percorrendo os predecessores do destino até a origem
        List<Vertice> caminho = new ArrayList<>();
        Vertice atual = destino;
        while (atual != null) {
            caminho.add(atual);
            atual = predecessores.get(atual);
        }
        Collections.reverse(caminho);

        return new Resultado(caminho, distancias.get(destino));
    }
}