package grafo;

/**
 * Popula o SistemaEmergencia com dados iniciais para demonstracao.
 * Representa uma cidade ficticia com hospitais, bases SAMU,
 * bairros, cruzamentos, vias e ambulancias pre-cadastradas.
 *
 * Uso: SeedDados.popular(sistema);
 */
public class SeedDados {

    /**
     * Popula o sistema com dados iniciais.
     *
     * @param sistema instancia do SistemaEmergencia a ser populada
     */
    public static void popular(SistemaEmergencia sistema) {
        popularHospitais(sistema);
        popularBasesSamu(sistema);
        popularBairrosECruzamentos(sistema);
        popularVias(sistema);
        popularAmbulancias(sistema);
        System.out.println("[Seed] Sistema populado com sucesso!");
        System.out.println("[Seed] Vertices: " + sistema.getGrafo().getVertices().size());
        System.out.println("[Seed] Arestas : " + sistema.getGrafo().getArestas().size());
        System.out.println("[Seed] Hospitais: " + sistema.getHospitais().size());
        System.out.println("[Seed] Ambulancias: " + sistema.getAmbulancias().size());
    }

    // ==================== Hospitais ====================

    private static void popularHospitais(SistemaEmergencia sistema) {
        sistema.cadastrarHospital(new Hospital(1, "Hospital Central",      -12.970, -38.510, 80, 40));
        sistema.cadastrarHospital(new Hospital(2, "UPA Norte",             -12.920, -38.460, 30, 10));
        sistema.cadastrarHospital(new Hospital(3, "Hospital Sul",          -13.020, -38.530, 50, 25));
        sistema.cadastrarHospital(new Hospital(4, "Pronto Socorro Leste",  -12.960, -38.440, 40, 38)); // quase lotado
        sistema.cadastrarHospital(new Hospital(5, "Hospital Universitario",-12.990, -38.490, 100, 60));
    }

    // ==================== Bases SAMU ====================

    private static void popularBasesSamu(SistemaEmergencia sistema) {
        sistema.cadastrarBaseSamu(new Vertice(10, "Base SAMU Centro",  TipoVertice.BASE_SAMU, -12.975, -38.505));
        sistema.cadastrarBaseSamu(new Vertice(11, "Base SAMU Norte",   TipoVertice.BASE_SAMU, -12.925, -38.465));
        sistema.cadastrarBaseSamu(new Vertice(12, "Base SAMU Sul",     TipoVertice.BASE_SAMU, -13.015, -38.525));
        sistema.cadastrarBaseSamu(new Vertice(13, "Base SAMU Leste",   TipoVertice.BASE_SAMU, -12.955, -38.445));
    }

    // ==================== Bairros e Cruzamentos ====================

    private static void popularBairrosECruzamentos(SistemaEmergencia sistema) {
        // Bairros
        sistema.cadastrarVertice(new Vertice(20, "Centro",          TipoVertice.BAIRRO,     -12.972, -38.508));
        sistema.cadastrarVertice(new Vertice(21, "Bairro Norte",    TipoVertice.BAIRRO,     -12.930, -38.462));
        sistema.cadastrarVertice(new Vertice(22, "Bairro Sul",      TipoVertice.BAIRRO,     -13.010, -38.522));
        sistema.cadastrarVertice(new Vertice(23, "Bairro Leste",    TipoVertice.BAIRRO,     -12.958, -38.448));
        sistema.cadastrarVertice(new Vertice(24, "Bairro Oeste",    TipoVertice.BAIRRO,     -12.968, -38.540));
        sistema.cadastrarVertice(new Vertice(25, "Vila Nova",       TipoVertice.BAIRRO,     -12.945, -38.500));
        sistema.cadastrarVertice(new Vertice(26, "Jardim America",  TipoVertice.BAIRRO,     -12.985, -38.475));
        sistema.cadastrarVertice(new Vertice(27, "Bairro Industrial",TipoVertice.BAIRRO,    -12.995, -38.455));

        // Cruzamentos
        sistema.cadastrarVertice(new Vertice(30, "Cruzamento A",    TipoVertice.CRUZAMENTO, -12.960, -38.495));
        sistema.cadastrarVertice(new Vertice(31, "Cruzamento B",    TipoVertice.CRUZAMENTO, -12.975, -38.478));
        sistema.cadastrarVertice(new Vertice(32, "Cruzamento C",    TipoVertice.CRUZAMENTO, -12.950, -38.510));
        sistema.cadastrarVertice(new Vertice(33, "Cruzamento D",    TipoVertice.CRUZAMENTO, -12.985, -38.498));
        sistema.cadastrarVertice(new Vertice(34, "Cruzamento E",    TipoVertice.CRUZAMENTO, -13.000, -38.510));
        sistema.cadastrarVertice(new Vertice(35, "Cruzamento F",    TipoVertice.CRUZAMENTO, -12.940, -38.480));
    }

    // ==================== Vias ====================

    private static void popularVias(SistemaEmergencia sistema) {
        GrafoCidade grafo = sistema.getGrafo();

        Vertice h1    = grafo.getVerticePorId(1);
        Vertice h2    = grafo.getVerticePorId(2);
        Vertice h3    = grafo.getVerticePorId(3);
        Vertice h4    = grafo.getVerticePorId(4);
        Vertice h5    = grafo.getVerticePorId(5);

        Vertice b10   = grafo.getVerticePorId(10);
        Vertice b11   = grafo.getVerticePorId(11);
        Vertice b12   = grafo.getVerticePorId(12);
        Vertice b13   = grafo.getVerticePorId(13);

        Vertice v20   = grafo.getVerticePorId(20); // Centro
        Vertice v21   = grafo.getVerticePorId(21); // Bairro Norte
        Vertice v22   = grafo.getVerticePorId(22); // Bairro Sul
        Vertice v23   = grafo.getVerticePorId(23); // Bairro Leste
        Vertice v24   = grafo.getVerticePorId(24); // Bairro Oeste
        Vertice v25   = grafo.getVerticePorId(25); // Vila Nova
        Vertice v26   = grafo.getVerticePorId(26); // Jardim America
        Vertice v27   = grafo.getVerticePorId(27); // Bairro Industrial

        Vertice cA    = grafo.getVerticePorId(30); // Cruzamento A
        Vertice cB    = grafo.getVerticePorId(31); // Cruzamento B
        Vertice cC    = grafo.getVerticePorId(32); // Cruzamento C
        Vertice cD    = grafo.getVerticePorId(33); // Cruzamento D
        Vertice cE    = grafo.getVerticePorId(34); // Cruzamento E
        Vertice cF    = grafo.getVerticePorId(35); // Cruzamento F

        // Bases -> Cruzamentos
        sistema.cadastrarViaBidirecional(b10, cA,  2.0);
        sistema.cadastrarViaBidirecional(b10, v20, 1.5);
        sistema.cadastrarViaBidirecional(b11, cF,  2.0);
        sistema.cadastrarViaBidirecional(b11, v21, 1.5);
        sistema.cadastrarViaBidirecional(b12, cE,  2.0);
        sistema.cadastrarViaBidirecional(b12, v22, 1.5);
        sistema.cadastrarViaBidirecional(b13, v23, 1.5);
        sistema.cadastrarViaBidirecional(b13, cB,  2.5);

        // Cruzamentos entre si
        sistema.cadastrarViaBidirecional(cA, cB,  3.0);
        sistema.cadastrarViaBidirecional(cA, cC,  2.5);
        sistema.cadastrarViaBidirecional(cA, cD,  4.0);
        sistema.cadastrarViaBidirecional(cB, cD,  3.5);
        sistema.cadastrarViaBidirecional(cC, cD,  3.0);
        sistema.cadastrarViaBidirecional(cD, cE,  4.5);
        sistema.cadastrarViaBidirecional(cE, cF,  5.0);  // via mais longa
        sistema.cadastrarViaBidirecional(cF, cA,  3.5);

        // Bairros -> Cruzamentos
        sistema.cadastrarViaBidirecional(v20, cA,  2.0);
        sistema.cadastrarViaBidirecional(v20, cC,  2.5);
        sistema.cadastrarViaBidirecional(v21, cF,  2.0);
        sistema.cadastrarViaBidirecional(v21, v25, 3.0);
        sistema.cadastrarViaBidirecional(v22, cE,  2.0);
        sistema.cadastrarViaBidirecional(v22, v26, 3.5);
        sistema.cadastrarViaBidirecional(v23, cB,  2.5);
        sistema.cadastrarViaBidirecional(v23, v27, 4.0);
        sistema.cadastrarViaBidirecional(v24, cC,  3.0);
        sistema.cadastrarViaBidirecional(v24, v20, 4.5);
        sistema.cadastrarViaBidirecional(v25, cA,  2.5);
        sistema.cadastrarViaBidirecional(v25, cD,  3.0);
        sistema.cadastrarViaBidirecional(v26, cD,  3.5);
        sistema.cadastrarViaBidirecional(v26, h5,  2.0);
        sistema.cadastrarViaBidirecional(v27, cE,  3.0);
        sistema.cadastrarViaBidirecional(v27, h4,  2.5);

        // Hospitais -> Cruzamentos/Bairros
        sistema.cadastrarViaBidirecional(h1, v20,  3.0);
        sistema.cadastrarViaBidirecional(h1, cD,   4.0);
        sistema.cadastrarViaBidirecional(h2, v21,  2.5);
        sistema.cadastrarViaBidirecional(h2, cF,   3.0);
        sistema.cadastrarViaBidirecional(h3, v22,  2.0);
        sistema.cadastrarViaBidirecional(h3, cE,   3.5);
        sistema.cadastrarViaBidirecional(h4, v23,  2.0);
        sistema.cadastrarViaBidirecional(h5, cD,   2.5);
        sistema.cadastrarViaBidirecional(h5, v26,  2.0);
    }

    // ==================== Ambulancias ====================

    private static void popularAmbulancias(SistemaEmergencia sistema) {
        GrafoCidade grafo = sistema.getGrafo();

        Vertice b10 = grafo.getVerticePorId(10); // Base SAMU Centro
        Vertice b11 = grafo.getVerticePorId(11); // Base SAMU Norte
        Vertice b12 = grafo.getVerticePorId(12); // Base SAMU Sul
        Vertice b13 = grafo.getVerticePorId(13); // Base SAMU Leste

        sistema.cadastrarAmbulancia(new Ambulancia(1, b10));
        sistema.cadastrarAmbulancia(new Ambulancia(2, b10));
        sistema.cadastrarAmbulancia(new Ambulancia(3, b11));
        sistema.cadastrarAmbulancia(new Ambulancia(4, b12));
        sistema.cadastrarAmbulancia(new Ambulancia(5, b13));
    }
}