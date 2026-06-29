package grafo;

/**
 * Enumeração dos status possíveis de uma ambulância.
 */
public enum StatusAmbulancia {
    DISPONIVEL("Disponível"),
    EM_ATENDIMENTO("Em atendimento");

    private final String descricao;

    StatusAmbulancia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
