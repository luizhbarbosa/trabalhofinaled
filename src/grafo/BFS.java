package grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import grafo.GrafoCidade;
import grafo.Vertice;

/**
 * Implementação do algoritmo BFS (Busca em Largura) para encontrar o caminho
 * com o menor número de arestas (cruzamentos) entre dois vértices no grafo da cidade.
 *
 * Usado para análise estrutural da rede viária e verificação de conectividade (T-08).
 */
public class BFS {

    /**
     * Classe que encapsula o resultado da busca BFS.
     */
    public static class Resultado {
        private final List<Vertice> caminho;
        private final int numeroArestas;

        /**
         * Construtor do resultado.
         *
         * @param caminho       lista de vértices que compõem o caminho (vazio se não houver rota)
         * @param numeroArestas número de arestas no caminho (ou -1 se não houver rota)
         */
        public Resultado(List<Vertice> caminho, int numeroArestas) {
            this.caminho = caminho;
            this.numeroArestas = numeroArestas;
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
         * Obtém o número de arestas (cruzamentos) no caminho.
         *
         * @return número de arestas, ou -1 se não houver rota
         */
        public int getNumeroArestas() {
            return numeroArestas;
        }

        /**
         * Verifica se foi encontrado um caminho válido.
         *
         * @return true se existe rota entre origem e destino
         */
        public boolean temCaminho() {
            return caminho != null && !caminho.isEmpty() && numeroArestas != -1;
        }
    }

    /**
     * Encontra o caminho com o menor número de arestas entre dois vértices usando o algoritmo BFS.
     *
     * @param grafo    o grafo da cidade contendo vértices e arestas
     * @param origem   vértice de origem do caminho
     * @param destino  vértice de destino do caminho
     * @return Resultado contendo o caminho e o número de arestas, ou caminho vazio se não existir rota
     */
    public static Resultado encontrarMenorCaminhoBFS(GrafoCidade grafo, Vertice origem, Vertice destino) {
        if (grafo == null || origem == null || destino == null) {
            return new Resultado(Collections.emptyList(), -1);
        }

        // Caso trivial: origem igual ao destino
        if (origem.equals(destino)) {
            return new Resultado(Collections.singletonList(origem), 0);
        }

        // Fila para a BFS
        Queue<Vertice> fila = new LinkedList<>();
        // Conjunto para marcar vértices visitados
        Set<Vertice> visitados = new HashSet<>();
        // Mapa para reconstruir o caminho
        Map<Vertice, Vertice> predecessores = new HashMap<>();
        // Mapa para armazenar o número de arestas até cada vértice
        Map<Vertice, Integer> distancias = new HashMap<>();

        fila.add(origem);
        visitados.add(origem);
        distancias.put(origem, 0);

        while (!fila.isEmpty()) {
            Vertice atual = fila.poll();

            // Se chegamos ao destino, podemos reconstruir o caminho
            if (atual.equals(destino)) {
                return reconstruirCaminho(origem, destino, predecessores, distancias.get(destino));
            }

            // Explora os vizinhos do vértice atual
            for (Vertice vizinho : grafo.getVizinhos(atual)) {
                if (!visitados.contains(vizinho)) {
                    visitados.add(vizinho);
                    predecessores.put(vizinho, atual);
                    distancias.put(vizinho, distancias.get(atual) + 1);
                    fila.add(vizinho);
                }
            }
        }

        // Destino não alcançável
        return new Resultado(Collections.emptyList(), -1);
    }

    /**
     * Reconstrói o caminho a partir dos predecessores.
     *
     * @param origem        vértice de origem
     * @param destino       vértice de destino
     * @param predecessores mapa de predecessores
     * @param numeroArestas número total de arestas no caminho
     * @return Resultado contendo o caminho e o número de arestas
     */
    private static Resultado reconstruirCaminho(Vertice origem, Vertice destino, Map<Vertice, Vertice> predecessores, int numeroArestas) {
        List<Vertice> caminho = new ArrayList<>();
        Vertice atual = destino;
        while (atual != null) {
            caminho.add(atual);
            atual = predecessores.get(atual);
        }
        Collections.reverse(caminho);
        return new Resultado(caminho, numeroArestas);
    }
}
