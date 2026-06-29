package grafo;

/**
 * Classe que representa um vértice no grafo da cidade.
 * Um vértice pode ser um hospital, base SAMU, bairro, cruzamento ou ponto de paciente.
 */
public class Vertice {
    private int id;
    private String nome;
    private TipoVertice tipo;
    private double latitude;
    private double longitude;

    /**
     * Construtor da classe Vertice.
     *
     * @param id        identificador único do vértice
     * @param nome      nome do vértice
     * @param tipo      tipo do vértice (Hospital, Base SAMU, Bairro, Cruzamento, Paciente)
     * @param latitude  coordenada de latitude
     * @param longitude coordenada de longitude
     */
    public Vertice(int id, String nome, TipoVertice tipo, double latitude, double longitude) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // ==================== Getters ====================

    /**
     * Obtém o identificador único do vértice.
     *
     * @return o id do vértice
     */
    public int getId() {
        return id;
    }

    /**
     * Obtém o nome do vértice.
     *
     * @return o nome do vértice
     */
    public String getNome() {
        return nome;
    }

    /**
     * Obtém o tipo do vértice.
     *
     * @return o tipo do vértice
     */
    public TipoVertice getTipo() {
        return tipo;
    }

    /**
     * Obtém a latitude do vértice.
     *
     * @return a latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Obtém a longitude do vértice.
     *
     * @return a longitude
     */
    public double getLongitude() {
        return longitude;
    }

    // ==================== Setters ====================

    /**
     * Define o nome do vértice.
     *
     * @param nome o novo nome
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Define o tipo do vértice.
     *
     * @param tipo o novo tipo
     */
    public void setTipo(TipoVertice tipo) {
        this.tipo = tipo;
    }

    /**
     * Define a latitude do vértice.
     *
     * @param latitude a nova latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Define a longitude do vértice.
     *
     * @param longitude a nova longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    // ==================== Métodos Utilitários ====================

    /**
     * Calcula a distância euclidiana entre este vértice e outro.
     * Usando aproximação de graus para quilômetros (1 grau ≈ 111 km).
     *
     * @param outro o outro vértice
     * @return a distância em quilômetros
     */
    public double calcularDistancia(Vertice outro) {
        double latDiff = this.latitude - outro.latitude;
        double lonDiff = this.longitude - outro.longitude;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111.0;
    }

    @Override
    public String toString() {
        return "Vertice{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", tipo=" + tipo.getDescricao() +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertice vertice = (Vertice) o;
        return id == vertice.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
