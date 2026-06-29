package grafo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Representa o grafo da cidade com vértices e arestas que conectam os pontos.
 */
public class GrafoCidade {
    private final Map<Integer, Vertice> vertices;
    private final List<Aresta> arestas;
    private final Map<Integer, List<Aresta>> adjacencias;

    public GrafoCidade() {
        this.vertices = new HashMap<>();
        this.arestas = new ArrayList<>();
        this.adjacencias = new HashMap<>();
    }

    /**
     * Adiciona um vértice ao grafo.
     *
     * @param vertice vértice a ser adicionado
     * @return true se o vértice foi adicionado, false se já existia
     */
    public boolean addVertice(Vertice vertice) {
        if (vertice == null || vertices.containsKey(vertice.getId())) {
            return false;
        }
        vertices.put(vertice.getId(), vertice);
        adjacencias.put(vertice.getId(), new ArrayList<>());
        return true;
    }

    /**
     * Adiciona uma aresta ao grafo.
     *
     * @param aresta aresta a ser adicionada
     * @return true se a aresta foi adicionada com sucesso
     */
    public boolean addAresta(Aresta aresta) {
        if (aresta == null) {
            return false;
        }
        Vertice origem = aresta.getOrigem();
        Vertice destino = aresta.getDestino();
        if (origem == null || destino == null
                || !vertices.containsKey(origem.getId())
                || !vertices.containsKey(destino.getId())) {
            return false;
        }
        if (arestas.contains(aresta)) {
            return false;
        }
        arestas.add(aresta);
        adjacencias.get(origem.getId()).add(aresta);
        return true;
    }

    /**
     * Retorna a lista de vizinhos diretos de um vértice.
     * Vizinhos são os destinos de arestas cuja origem é o vértice informado.
     *
     * @param vertice vértice de origem
     * @return lista de vértices vizinhos
     */
    public List<Vertice> getVizinhos(Vertice vertice) {
        if (vertice == null || !adjacencias.containsKey(vertice.getId())) {
            return new ArrayList<>();
        }
        return adjacencias.get(vertice.getId()).stream()
                .filter(aresta -> !aresta.estaBloqueada())
                .map(Aresta::getDestino)
                .collect(Collectors.toList());
    }

    /**
     * Remove uma aresta do grafo.
     *
     * @param aresta aresta a ser removida
     * @return true se a aresta foi removida, false caso contrário
     */
    public boolean removerAresta(Aresta aresta) {
        if (aresta == null || !arestas.contains(aresta)) {
            return false;
        }
        boolean removido = arestas.remove(aresta);
        List<Aresta> adj = adjacencias.get(aresta.getOrigem().getId());
        if (adj != null) {
            adj.remove(aresta);
        }
        return removido;
    }

    /**
     * Retorna uma cópia da lista de vértices do grafo.
     *
     * @return lista de vértices
     */
    /**
     * Encontra o caminho com o menor número de arestas entre dois vértices usando o algoritmo BFS.
     * Este método é um wrapper para o algoritmo BFS implementado na classe `BFS`.
     *
     * @param origem   vértice de origem do caminho
     * @param destino  vértice de destino do caminho
     * @return Resultado contendo o caminho e o número de arestas, ou caminho vazio se não existir rota
     */
    public BFS.Resultado encontrarCaminhoBFS(Vertice origem, Vertice destino) {
        return BFS.encontrarMenorCaminhoBFS(this, origem, destino);
    }

    public List<Vertice> getVertices() {
        return new ArrayList<>(vertices.values());
    }

    /**
     * Retorna uma cópia da lista de arestas do grafo.
     *
     * @return lista de arestas
     */
    public List<Aresta> getArestas() {
        return new ArrayList<>(arestas);
    }

    /**
     * Tenta localizar um vértice pelo seu id.
     *
     * @param id identificador do vértice
     * @return o vértice ou null se não existir
     */
    public Vertice getVerticePorId(int id) {
        return vertices.get(id);
    }

    @Override
    public String toString() {
        return "GrafoCidade{" +
                "vertices=" + vertices.values() +
                ", arestas=" + arestas +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrafoCidade that = (GrafoCidade) o;
        return Objects.equals(vertices, that.vertices) && Objects.equals(arestas, that.arestas);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices, arestas);
    }
}
