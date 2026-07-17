package grafo;

import java.util.*;

/**
 * Implementação do algoritmo de Componentes Conexos usando BFS.
 * Identifica regiões isoladas da rede viária que não possuem acesso a hospitais (RF12).
 *
 * Um componente conexo é um subconjunto de vértices onde todos estão
 * interligados entre si, mas sem conexão com os demais componentes.
 */
public class ComponentesConexos {

    /**
     * Classe que representa um componente conexo da rede.
     */
    public static class Componente {
        private final int id;
        private final List<Vertice> vertices;

        public Componente(int id, List<Vertice> vertices) {
            this.id = id;
            this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
        }

        public int getId() {
            return id;
        }

        public List<Vertice> getVertices() {
            return vertices;
        }

        public int getTamanho() {
            return vertices.size();
        }

        /**
         * Verifica se este componente possui pelo menos um hospital disponível.
         *
         * @return true se houver hospital acessível no componente
         */
        public boolean temHospital() {
            return vertices.stream().anyMatch(v ->
                    v.getTipo() == TipoVertice.HOSPITAL &&
                    v instanceof Hospital &&
                    ((Hospital) v).isDisponivel()
            );
        }

        /**
         * Verifica se este componente está isolado (sem acesso a hospital).
         *
         * @return true se o componente não tiver hospital disponível
         */
        public boolean estaIsolado() {
            return !temHospital();
        }

        @Override
        public String toString() {
            return "Componente{" +
                    "id=" + id +
                    ", tamanho=" + getTamanho() +
                    ", temHospital=" + temHospital() +
                    ", vertices=" + vertices.stream()
                            .map(Vertice::getNome)
                            .collect(java.util.stream.Collectors.toList()) +
                    '}';
        }
    }

    /**
     * Classe que encapsula o resultado da análise de componentes conexos.
     */
    public static class Resultado {
        private final List<Componente> componentes;

        public Resultado(List<Componente> componentes) {
            this.componentes = Collections.unmodifiableList(new ArrayList<>(componentes));
        }

        public List<Componente> getComponentes() {
            return componentes;
        }

        public int getQuantidadeComponentes() {
            return componentes.size();
        }

        /**
         * Retorna componentes sem acesso a hospital (regiões isoladas).
         *
         * @return lista de componentes isolados
         */
        public List<Componente> getComponentesIsolados() {
            return componentes.stream()
                    .filter(Componente::estaIsolado)
                    .collect(java.util.stream.Collectors.toList());
        }

        /**
         * Verifica se a rede é totalmente conexa (um único componente).
         *
         * @return true se todos os vértices estão interligados
         */
        public boolean eConexo() {
            return componentes.size() == 1;
        }

        @Override
        public String toString() {
            return "Resultado{" +
                    "totalComponentes=" + getQuantidadeComponentes() +
                    ", isolados=" + getComponentesIsolados().size() +
                    ", conexo=" + eConexo() +
                    '}';
        }
    }

    /**
     * Identifica todos os componentes conexos do grafo usando BFS.
     * Considera o grafo como não-dirigido para fins de conectividade:
     * dois vértices pertencem ao mesmo componente se há caminho entre eles
     * em qualquer direção (ignorando vias bloqueadas).
     *
     * @param grafo o grafo da cidade
     * @return Resultado contendo todos os componentes identificados
     */
    public static Resultado identificarComponentes(GrafoCidade grafo) {
        if (grafo == null) {
            return new Resultado(Collections.emptyList());
        }

        List<Vertice> todosVertices = grafo.getVertices();
        Set<Vertice> visitados = new HashSet<>();
        List<Componente> componentes = new ArrayList<>();
        int idComponente = 0;

        // Monta mapa de adjacência não-dirigida (ignora vias bloqueadas)
        Map<Vertice, Set<Vertice>> adjacenciaNaoDirigida = construirAdjacenciaNaoDirigida(grafo);

        for (Vertice inicio : todosVertices) {
            if (visitados.contains(inicio)) continue;

            // BFS para explorar todo o componente a partir deste vértice
            List<Vertice> componenteAtual = new ArrayList<>();
            Queue<Vertice> fila = new LinkedList<>();

            fila.add(inicio);
            visitados.add(inicio);

            while (!fila.isEmpty()) {
                Vertice atual = fila.poll();
                componenteAtual.add(atual);

                for (Vertice vizinho : adjacenciaNaoDirigida.getOrDefault(atual, Collections.emptySet())) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        fila.add(vizinho);
                    }
                }
            }

            componentes.add(new Componente(idComponente++, componenteAtual));
        }

        return new Resultado(componentes);
    }

    /**
     * Constrói um mapa de adjacência não-dirigida a partir do grafo dirigido.
     * Ignora arestas bloqueadas.
     *
     * @param grafo o grafo da cidade
     * @return mapa de adjacência não-dirigida
     */
    private static Map<Vertice, Set<Vertice>> construirAdjacenciaNaoDirigida(GrafoCidade grafo) {
        Map<Vertice, Set<Vertice>> adj = new HashMap<>();

        for (Vertice v : grafo.getVertices()) {
            adj.put(v, new HashSet<>());
        }

        for (Aresta aresta : grafo.getArestas()) {
            if (aresta.estaBloqueada()) continue;

            Vertice origem = aresta.getOrigem();
            Vertice destino = aresta.getDestino();

            adj.computeIfAbsent(origem, k -> new HashSet<>()).add(destino);
            adj.computeIfAbsent(destino, k -> new HashSet<>()).add(origem);
        }

        return adj;
    }
}