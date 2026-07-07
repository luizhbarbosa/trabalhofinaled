package grafo;

/**
 * Ponto de entrada do sistema de emergencia SAMU.
 * Popula o sistema via SeedDados e executa um fluxo de teste completo.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("===========================================");
        System.out.println("   SISTEMA DE EMERGENCIA SAMU             ");
        System.out.println("===========================================\n");

        // Inicializa e popula o sistema
        GrafoCidade grafo = new GrafoCidade();
        SistemaEmergencia sistema = new SistemaEmergencia(grafo);
        SeedDados.popular(sistema);

        // ==================== Etapa 1: Registrar ocorrencia ====================
        // registrarOcorrencia() agora garante RN05 automaticamente: conecta o
        // paciente ao grafo, localiza ambulancia, calcula rota e seleciona
        // hospital em uma unica chamada.

        System.out.println("\n-------------------------------------------");
        System.out.println("ETAPA 1 - Registrando ocorrencia...");

        Paciente paciente = new Paciente(99, "Joao Silva", -12.962, -38.497,
                NivelUrgencia.ALTA, "Dor no peito intensa");

        SistemaEmergencia.AtendimentoResultado atendimento = sistema.registrarOcorrencia(paciente);

        if (atendimento == null) {
            System.out.println("ERRO: Falha ao registrar ocorrencia (paciente invalido ou id duplicado).");
            return;
        }

        System.out.println("OK Paciente: " + paciente.getNome()
                + " | Urgencia: " + paciente.getNivelUrgencia().getDescricao()
                + " | Ocorrencia: " + paciente.getDescricaoOcorrencia());
        System.out.println("  " + atendimento.getMensagem());

        if (!atendimento.isSucesso()) {
            System.out.println("ERRO: " + atendimento.getMensagem());
            return;
        }

        Ambulancia ambulancia = atendimento.getAmbulancia();
        Dijkstra.Resultado rotaAmbulancia = atendimento.getRotaAmbulancia();
        AEstrela.Resultado rotaHospital = atendimento.getRotaHospital();

        // ==================== Etapa 2: Ambulancia localizada ====================

        System.out.println("\n-------------------------------------------");
        System.out.println("ETAPA 2 - Ambulancia localizada...");
        System.out.println("OK Ambulancia #" + ambulancia.getId()
                + " em " + ambulancia.getLocalizacaoAtual().getNome());

        // ==================== Etapa 3: Rota ambulancia -> paciente ====================

        System.out.println("\n-------------------------------------------");
        System.out.println("ETAPA 3 - Rota ambulancia -> paciente (Dijkstra)...");

        imprimirCaminho(rotaAmbulancia.getCaminho());
        System.out.println("  ETA: " + sistema.estimarTempoChegada(rotaAmbulancia.getCustoTotal()));
        sistema.despacharAmbulancia(ambulancia);
        System.out.println("OK Ambulancia despachada. Status: " + ambulancia.getStatus().getDescricao());

        // ==================== Etapa 4: Hospital mais proximo ====================

        System.out.println("\n-------------------------------------------");
        System.out.println("ETAPA 4 - Hospital destino (A*)...");

        if (!rotaHospital.temCaminho()) {
            System.out.println("ERRO: Nenhum hospital disponivel!");
        } else {
            Vertice destino = rotaHospital.getCaminho().get(rotaHospital.getCaminho().size() - 1);
            System.out.println("OK Hospital: " + destino.getNome());
            imprimirCaminho(rotaHospital.getCaminho());
            System.out.println("  ETA: " + sistema.estimarTempoChegada(rotaHospital.getCustoTotal()));
        }

        // ==================== Etapa 5: Bloqueio e recalculo ====================

        System.out.println("\n-------------------------------------------");
        System.out.println("ETAPA 5 - Simulando bloqueio de via...");

        Aresta arestaBloquear = null;
        for (Aresta a : grafo.getArestas()) {
            if (a.getOrigem().getId() == 30 && a.getDestino().getId() == 31) {
                arestaBloquear = a;
                break;
            }
        }

        if (arestaBloquear != null) {
            System.out.println("Bloqueando: " + arestaBloquear.getOrigem().getNome()
                    + " -> " + arestaBloquear.getDestino().getNome());
            RecalculoRota.Resultado recalculo = sistema.recalcularRota(
                    arestaBloquear,
                    rotaAmbulancia.getCaminho(),
                    ambulancia.getLocalizacaoAtual(),
                    paciente
            );
            System.out.println("  " + recalculo.getMensagem());
            if (recalculo.isRotaRecalculada() && recalculo.temCaminho()) {
                imprimirCaminho(recalculo.getCaminho());
            }
        }

        // ==================== Etapa 6: Analise de cobertura ====================

        System.out.println("\n-------------------------------------------");
        System.out.println("ETAPA 6 - Analise de cobertura da rede...");

        ComponentesConexos.Resultado cobertura = sistema.analisarCobertura();
        System.out.println("  Componentes: " + cobertura.getQuantidadeComponentes());
        System.out.println("  Rede conexa: " + (cobertura.eConexo() ? "Sim" : "Nao"));
        if (!cobertura.getComponentesIsolados().isEmpty()) {
            System.out.println("  AVISO - Regioes sem hospital:");
            for (ComponentesConexos.Componente c : cobertura.getComponentesIsolados()) {
                System.out.println("    Componente " + c.getId() + " (" + c.getTamanho() + " vertices)");
            }
        } else {
            System.out.println("  Todas as regioes tem acesso a hospital.");
        }

        // ==================== Finalizar ====================

        System.out.println("\n-------------------------------------------");
        System.out.println("FINALIZANDO atendimento...");

        Vertice hospitalFinal = rotaHospital.temCaminho()
                ? rotaHospital.getCaminho().get(rotaHospital.getCaminho().size() - 1)
                : sistema.getHospitais().get(0);

        sistema.finalizarAtendimento(ambulancia, hospitalFinal);
        System.out.println("OK Atendimento finalizado.");
        System.out.println("  Ambulancia em: " + ambulancia.getLocalizacaoAtual().getNome());
        System.out.println("  Status: " + ambulancia.getStatus().getDescricao());

        System.out.println("\n===========================================");
        System.out.println("           CONCLUIDO                      ");
        System.out.println("===========================================");
    }

    private static void imprimirCaminho(java.util.List<Vertice> caminho) {
        StringBuilder sb = new StringBuilder("  Caminho: ");
        for (int i = 0; i < caminho.size(); i++) {
            sb.append(caminho.get(i).getNome());
            if (i < caminho.size() - 1) sb.append(" -> ");
        }
        System.out.println(sb);
    }
}