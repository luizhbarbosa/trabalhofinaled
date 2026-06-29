package grafo;

/**
 * Classe que representa uma aresta (via/rua) no grafo da cidade.
 * Uma aresta conecta dois vértices e possui um peso (tempo de deslocamento) e um status.
 */
public class Aresta {
    private Vertice origem;
    private Vertice destino;
    private double peso; // Tempo de deslocamento em minutos
    private StatusVia status;

    /**
     * Construtor da classe Aresta.
     *
     * @param origem  vértice de origem da aresta
     * @param destino vértice de destino da aresta
     * @param peso    peso da aresta (tempo de deslocamento em minutos)
     * @param status  status da via (livre, bloqueada, congestionada)
     */
    public Aresta(Vertice origem, Vertice destino, double peso, StatusVia status) {
        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
        this.status = status;
    }

    /**
     * Construtor da classe Aresta com status padrão LIVRE.
     *
     * @param origem  vértice de origem da aresta
     * @param destino vértice de destino da aresta
     * @param peso    peso da aresta (tempo de deslocamento em minutos)
     */
    public Aresta(Vertice origem, Vertice destino, double peso) {
        this(origem, destino, peso, StatusVia.LIVRE);
    }

    // ==================== Getters ====================

    /**
     * Obtém o vértice de origem da aresta.
     *
     * @return o vértice de origem
     */
    public Vertice getOrigem() {
        return origem;
    }

    /**
     * Obtém o vértice de destino da aresta.
     *
     * @return o vértice de destino
     */
    public Vertice getDestino() {
        return destino;
    }

    /**
     * Obtém o peso da aresta.
     *
     * @return o peso (tempo de deslocamento em minutos)
     */
    public double getPeso() {
        return peso;
    }

    /**
     * Obtém o status da via.
     *
     * @return o status da via
     */
    public StatusVia getStatus() {
        return status;
    }

    /**
     * Calcula o peso efetivo da aresta considerando seu status.
     * Vias bloqueadas retornam infinito; vias congestionadas têm seu peso aumentado.
     *
     * @return o peso efetivo da aresta
     */
    public double getPesoEfetivo() {
        return peso * status.getMultiplicador();
    }

    // ==================== Setters ====================

    /**
     * Define o peso da aresta.
     *
     * @param peso o novo peso (tempo de deslocamento em minutos)
     */
    public void setPeso(double peso) {
        this.peso = peso;
    }

    /**
     * Define o status da via.
     *
     * @param status o novo status
     */
    public void setStatus(StatusVia status) {
        this.status = status;
    }

    // ==================== Métodos Utilitários ====================

    /**
     * Verifica se a aresta está bloqueada (intransitável).
     *
     * @return true se a via está bloqueada
     */
    public boolean estaBloqueada() {
        return status == StatusVia.BLOQUEADA;
    }

    /**
     * Verifica se a aresta está congestionada.
     *
     * @return true se a via está congestionada
     */
    public boolean estaCongestionada() {
        return status == StatusVia.CONGESTIONADA;
    }

    /**
     * Verifica se a aresta está livre (sem obstáculos).
     *
     * @return true se a via está livre
     */
    public boolean estaLivre() {
        return status == StatusVia.LIVRE;
    }

    @Override
    public String toString() {
        return "Aresta{" +
                "origem=" + origem.getNome() +
                " (id=" + origem.getId() + ")" +
                ", destino=" + destino.getNome() +
                " (id=" + destino.getId() + ")" +
                ", peso=" + peso +
                "min, status=" + status.getDescricao() +
                ", pesoEfetivo=" + getPesoEfetivo() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aresta aresta = (Aresta) o;
        return origem.getId() == aresta.origem.getId() &&
                destino.getId() == aresta.destino.getId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(origem.getId() * 31 + destino.getId());
    }
}
