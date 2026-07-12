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

    public boolean addVertice(Vertice vertice) {
        if (vertice == null || vertices.containsKey(vertice.getId())) {
            return false;
        }
        vertices.put(vertice.getId(), vertice);
        adjacencias.put(vertice.getId(), new ArrayList<>());
        return true;
    }

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
     * Retorna a lista de vizinhos diretos de um vértice (ignora vias bloqueadas).
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
 * Retorna todos os hospitais cadastrados no grafo.
 *
 * @return lista de hospitais
 */
public List<Hospital> getHospitais() {
    List<Hospital> hospitais = new ArrayList<>();

    for (Vertice v : vertices.values()) {
        if (v instanceof Hospital hospital) {
            hospitais.add(hospital);
        }
    }

    return hospitais;
}
    /**
     * Retorna as arestas que partem de um vértice (lista de adjacência).
     * Usado pelos algoritmos de busca para evitar percorrer todas as arestas do grafo.
     *
     * @param vertice vértice de origem
     * @return lista de arestas que saem do vértice, ou lista vazia se o vértice não existir
     */
    public List<Aresta> getArestasSaida(Vertice vertice) {
        if (vertice == null || !adjacencias.containsKey(vertice.getId())) {
            return new ArrayList<>();
        }
        return new ArrayList<>(adjacencias.get(vertice.getId()));
    }

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

    public BFS.Resultado encontrarCaminhoBFS(Vertice origem, Vertice destino) {
        return BFS.encontrarMenorCaminhoBFS(this, origem, destino);
    }

    public List<Vertice> getVertices() {
        return new ArrayList<>(vertices.values());
    }

    public List<Aresta> getArestas() {
        return new ArrayList<>(arestas);
    }

        public Vertice getVerticePorId(int id) {
        return vertices.get(id);
    }

    /**
     * Remove um vértice do grafo, junto com todas as arestas que
     * partem dele ou chegam nele (evita arestas "fantasma" apontando
     * para um vértice inexistente).
     *
     * @param vertice vértice a remover
     * @return true se removido, false se o vértice não existia
     */
    public boolean removerVertice(Vertice vertice) {
        if (vertice == null || !vertices.containsKey(vertice.getId())) {
            return false;
        }
        int id = vertice.getId();
        arestas.removeIf(a -> a.getOrigem().getId() == id || a.getDestino().getId() == id);
        for (List<Aresta> lista : adjacencias.values()) {
            lista.removeIf(a -> a.getDestino().getId() == id);
        }
        adjacencias.remove(id);
        vertices.remove(id);
        return true;
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