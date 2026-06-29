package grafo;

/**
 * Enumeração dos níveis de urgência de um paciente.
 */
public enum NivelUrgencia {
    BAIXA("Baixa"),
    MEDIA("Média"),
    ALTA("Alta"),
    CRITICA("Crítica");

    private final String descricao;

    NivelUrgencia(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
