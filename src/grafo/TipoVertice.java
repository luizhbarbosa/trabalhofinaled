package grafo;

/**
 * Enumeração dos tipos de vértice no sistema de mapeamento de rotas para ambulâncias.
 */
public enum TipoVertice {
    HOSPITAL("Hospital"),
    BASE_SAMU("Base SAMU"),
    BAIRRO("Bairro"),
    CRUZAMENTO("Cruzamento"),
    PACIENTE("Paciente");

    private final String descricao;

    TipoVertice(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
