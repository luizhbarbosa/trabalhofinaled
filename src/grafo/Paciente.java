package grafo;

/**
 * Representa um paciente no sistema de emergência.
 * Os pacientes também são vértices no grafo, com um nível de urgência.
 */
public class Paciente extends Vertice {
    private NivelUrgencia nivelUrgencia;
    private String descricaoOcorrencia;

    /**
     * Construtor do paciente.
     *
     * @param id                 identificador único do paciente
     * @param nome               nome ou identificação do paciente
     * @param latitude           coordenada de latitude da ocorrência
     * @param longitude          coordenada de longitude da ocorrência
     * @param nivelUrgencia      nível de urgência do atendimento
     * @param descricaoOcorrencia descrição opcional da ocorrência
     */
    public Paciente(int id, String nome, double latitude, double longitude,
                    NivelUrgencia nivelUrgencia, String descricaoOcorrencia) {
        super(id, nome, TipoVertice.PACIENTE, latitude, longitude);
        this.nivelUrgencia = nivelUrgencia != null ? nivelUrgencia : NivelUrgencia.MEDIA;
        this.descricaoOcorrencia = descricaoOcorrencia != null ? descricaoOcorrencia : "";
    }

    /**
     * Obtém o nível de urgência do paciente.
     *
     * @return o nível de urgência
     */
    public NivelUrgencia getNivelUrgencia() {
        return nivelUrgencia;
    }

    /**
     * Define o nível de urgência do paciente.
     *
     * @param nivelUrgencia novo nível de urgência
     */
    public void setNivelUrgencia(NivelUrgencia nivelUrgencia) {
        this.nivelUrgencia = nivelUrgencia != null ? nivelUrgencia : NivelUrgencia.MEDIA;
    }

    /**
     * Obtém a descrição da ocorrência.
     *
     * @return descrição da ocorrência
     */
    public String getDescricaoOcorrencia() {
        return descricaoOcorrencia;
    }

    /**
     * Define a descrição da ocorrência.
     *
     * @param descricaoOcorrencia nova descrição da ocorrência
     */
    public void setDescricaoOcorrencia(String descricaoOcorrencia) {
        this.descricaoOcorrencia = descricaoOcorrencia != null ? descricaoOcorrencia : "";
    }

    @Override
    public String toString() {
        return "Paciente{" +
                "id=" + getId() +
                ", nome='" + getNome() + '\'' +
                ", nivelUrgencia=" + nivelUrgencia.getDescricao() +
                ", descricaoOcorrencia='" + descricaoOcorrencia + '\'' +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                '}';
    }
}
