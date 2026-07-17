package grafo;

/**
 * Classe que representa uma aresta (via/rua) no grafo da cidade.
 * Uma aresta conecta dois vértices e possui um peso (tempo de deslocamento), status e nome.
 */
public class Aresta {
    private Vertice origem;
    private Vertice destino;
    private double peso; // Tempo de deslocamento em minutos
    private StatusVia status;
    private String nome; // NOVO: Atributo para guardar o nome da rua/avenida

    /**
     * Construtor completo da classe Aresta.
     *
     * @param origem  vértice de origem da aresta
     * @param destino vértice de destino da aresta
     * @param peso    peso da aresta (tempo de deslocamento em minutos)
     * @param status  status da via (livre, bloqueada, congestionada)
     * @param nome    nome da via/rua
     */
    public Aresta(Vertice origem, Vertice destino, double peso, StatusVia status, String nome) {
        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
        this.status = status;
        this.nome = (nome != null && !nome.trim().isEmpty()) ? nome : "Via " + origem.getNome() + " - " + destino.getNome();
    }

    /**
     * Construtor da classe Aresta sem o nome (gera um nome automático baseado nos vértices).
     */
    public Aresta(Vertice origem, Vertice destino, double peso, StatusVia status) {
        this(origem, destino, peso, status, "Via " + origem.getNome() + " - " + destino.getNome());
    }

    /**
     * Construtor simplificado com status padrão LIVRE e nome personalizado.
     */
    public Aresta(Vertice origem, Vertice destino, double peso, String nome) {
        this(origem, destino, peso, StatusVia.LIVRE, nome);
    }

    // ==================== Getters ====================

    /**
     * Obtém o nome da via/rua.
     *
     * @return o nome da via
     */
    public String getNome() {
        return nome;
    }

    public Vertice getOrigem() {
        return origem;
    }

    public Vertice getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public StatusVia getStatus() {
        return status;
    }

    public double getPesoEfetivo() {
        return peso * status.getMultiplicador();
    }

    // ==================== Setters ====================

    /**
     * Define o nome da via/rua.
     *
     * @param nome o novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public void setStatus(StatusVia status) {
        this.status = status;
    }

    // ==================== Métodos Utilitários ====================

    public boolean estaBloqueada() {
        return status == StatusVia.BLOQUEADA;
    }

    public boolean estaCongestionada() {
        return status == StatusVia.CONGESTIONADA;
    }

    public boolean estaLivre() {
        return status == StatusVia.LIVRE;
    }

    @Override
    public String toString() {
        return "Aresta{" +
                "nome='" + nome + '\'' +
                ", origem=" + origem.getNome() +
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