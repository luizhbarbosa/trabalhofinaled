package grafo;

/**
 * Enumeração dos possíveis estados de uma via (aresta) no grafo da cidade.
 */
public enum StatusVia {
    LIVRE("Livre", 1.0),
    CONGESTIONADA("Congestionada", 1.5),
    BLOQUEADA("Bloqueada", Double.POSITIVE_INFINITY);

    private final String descricao;
    private final double multiplicador; // Multiplicador de peso para o status

    StatusVia(String descricao, double multiplicador) {
        this.descricao = descricao;
        this.multiplicador = multiplicador;
    }

    public String getDescricao() {
        return descricao;
    }

    /**
     * Retorna o multiplicador de peso para este status.
     * LIVRE: 1.0 (sem alteração)
     * CONGESTIONADA: 1.5 (aumenta 50%)
     * BLOQUEADA: POSITIVE_INFINITY (via intransitável)
     *
     * @return o multiplicador de peso
     */
    public double getMultiplicador() {
        return multiplicador;
    }
}
