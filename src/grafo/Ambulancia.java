package grafo;

/**
 * Representa uma ambulância no sistema de emergência.
 * Possui localização atual, status de atendimento e uma ocorrência por vez.
 */
public class Ambulancia {
    private int id;
    private Vertice localizacaoAtual;
    private StatusAmbulancia status;
    private boolean atendendo;

    public Ambulancia(int id, Vertice localizacaoAtual) {
        this.id = id;
        this.localizacaoAtual = localizacaoAtual;
        this.status = StatusAmbulancia.DISPONIVEL;
        this.atendendo = false;
    }

    public int getId() {
        return id;
    }

    public Vertice getLocalizacaoAtual() {
        return localizacaoAtual;
    }

    public void setLocalizacaoAtual(Vertice localizacaoAtual) {
        this.localizacaoAtual = localizacaoAtual;
    }

    public StatusAmbulancia getStatus() {
        return status;
    }

    public void setStatus(StatusAmbulancia status) {
        this.status = status;
        this.atendendo = status == StatusAmbulancia.EM_ATENDIMENTO;
    }

    public boolean isDisponivel() {
        return status == StatusAmbulancia.DISPONIVEL && !atendendo;
    }

    public boolean estaEmAtendimento() {
        return status == StatusAmbulancia.EM_ATENDIMENTO;
    }

    public boolean iniciarAtendimento() {
        if (!isDisponivel()) {
            return false;
        }
        setStatus(StatusAmbulancia.EM_ATENDIMENTO);
        return true;
    }

    public boolean finalizarAtendimento() {
        if (!estaEmAtendimento()) {
            return false;
        }
        setStatus(StatusAmbulancia.DISPONIVEL);
        return true;
    }

    @Override
    public String toString() {
        return "Ambulancia{" +
                "id=" + id +
                ", localizacaoAtual=" + (localizacaoAtual != null ? localizacaoAtual.getNome() : "n/a") +
                ", status=" + status.getDescricao() +
                '}';
    }
}
