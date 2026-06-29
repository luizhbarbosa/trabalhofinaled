package grafo;

/**
 * Representa um hospital no grafo da cidade.
 * Herda de Vertice e adiciona lógica de capacidade e ocupação.
 */
public class Hospital extends Vertice {
    private int capacidadeMaxima;
    private int ocupacaoAtual;

    /**
     * Construtor do hospital.
     *
     * @param id              identificador único do hospital
     * @param nome            nome do hospital
     * @param latitude        coordenada de latitude
     * @param longitude       coordenada de longitude
     * @param capacidadeMaxima capacidade máxima de atendimento do hospital
     * @param ocupacaoAtual   ocupação atual do hospital
     */
    public Hospital(int id, String nome, double latitude, double longitude, int capacidadeMaxima, int ocupacaoAtual) {
        super(id, nome, TipoVertice.HOSPITAL, latitude, longitude);
        this.capacidadeMaxima = Math.max(0, capacidadeMaxima);
        this.ocupacaoAtual = Math.min(Math.max(0, ocupacaoAtual), this.capacidadeMaxima);
    }

    /**
     * Verifica se o hospital tem vagas disponíveis.
     *
     * @return true se ocupação atual for menor que a capacidade máxima
     */
    public boolean isDisponivel() {
        return ocupacaoAtual < capacidadeMaxima;
    }

    /**
     * Obtém a capacidade máxima do hospital.
     *
     * @return capacidade máxima
     */
    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    /**
     * Define a capacidade máxima do hospital.
     *
     * @param capacidadeMaxima nova capacidade máxima
     */
    public void setCapacidadeMaxima(int capacidadeMaxima) {
        this.capacidadeMaxima = Math.max(0, capacidadeMaxima);
        if (ocupacaoAtual > this.capacidadeMaxima) {
            ocupacaoAtual = this.capacidadeMaxima;
        }
    }

    /**
     * Obtém a ocupação atual.
     *
     * @return ocupação atual
     */
    public int getOcupacaoAtual() {
        return ocupacaoAtual;
    }

    /**
     * Define a ocupação atual.
     *
     * @param ocupacaoAtual nova ocupação atual
     */
    public void setOcupacaoAtual(int ocupacaoAtual) {
        this.ocupacaoAtual = Math.min(Math.max(0, ocupacaoAtual), capacidadeMaxima);
    }

    /**
     * Incrementa a ocupação atual em 1, se houver vaga.
     *
     * @return true se a ocupação foi incrementada, false se o hospital já está cheio
     */
    public boolean admitirPaciente() {
        if (!isDisponivel()) {
            return false;
        }
        ocupacaoAtual++;
        return true;
    }

    /**
     * Decrementa a ocupação atual em 1, se houver pacientes internados.
     *
     * @return true se a ocupação foi decrementada, false se já estiver em zero
     */
    public boolean liberarVaga() {
        if (ocupacaoAtual <= 0) {
            return false;
        }
        ocupacaoAtual--;
        return true;
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", capacidadeMaxima=" + capacidadeMaxima +
                ", ocupacaoAtual=" + ocupacaoAtual +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                '}';
    }
}
