package grafo;

/**
 * Ponto único de acesso ao backend para todas as telas Swing.
 * Garante que TODAS as telas (Hospital, Via, Atendimento, Principal)
 * compartilhem a MESMA instância de GrafoCidade / SistemaEmergencia,
 * em vez de cada uma recriar o grafo do zero.
 *
 * Espelha exatamente a inicialização feita em Main.java.
 *
 * Uso em qualquer tela:
 *   GrafoCidade grafo = AppContext.getInstancia().getGrafo();
 *   SistemaEmergencia sistema = AppContext.getInstancia().getSistemaEmergencia();
 */
public class AppContext {

    private static AppContext instancia;

    private final GrafoCidade grafo;
    private final SistemaEmergencia sistemaEmergencia;

    private AppContext() {
        this.grafo = new GrafoCidade();
        this.sistemaEmergencia = new SistemaEmergencia(grafo);
        SeedDados.popular(sistemaEmergencia);
    }

    public static synchronized AppContext getInstancia() {
        if (instancia == null) {
            instancia = new AppContext();
        }
        return instancia;
    }

    public GrafoCidade getGrafo() {
        return grafo;
    }

    public SistemaEmergencia getSistemaEmergencia() {
        return sistemaEmergencia;
    }
}