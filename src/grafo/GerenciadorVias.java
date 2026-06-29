package grafo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Serviço responsável por gerenciar pesos dinâmicos nas arestas do grafo.
 * Permite bloquear, congestionar e liberar vias, atualizando os pesos
 * automaticamente conforme o status (RN03, RN04, RF09).
 */
public class GerenciadorVias {

    /**
     * Classe que encapsula o resultado de uma alteração de via.
     */
    public static class Resultado {
        private final boolean sucesso;
        private final String mensagem;
        private final Aresta aresta;

        public Resultado(boolean sucesso, String mensagem, Aresta aresta) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
            this.aresta = aresta;
        }

        public boolean isSucesso() {
            return sucesso;
        }

        public String getMensagem() {
            return mensagem;
        }

        public Aresta getAresta() {
            return aresta;
        }

        @Override
        public String toString() {
            return "GerenciadorVias.Resultado{" +
                    "sucesso=" + sucesso +
                    ", mensagem='" + mensagem + '\'' +
                    '}';
        }
    }

    /**
     * Bloqueia uma via, tornando-a intransitável (RN03).
     * O peso efetivo passa a ser infinito.
     *
     * @param grafo  o grafo da cidade
     * @param aresta a aresta a ser bloqueada
     * @return Resultado da operação
     */
    public static Resultado bloquearVia(GrafoCidade grafo, Aresta aresta) {
        if (grafo == null || aresta == null) {
            return new Resultado(false, "Parâmetros inválidos.", null);
        }
        if (!grafo.getArestas().contains(aresta)) {
            return new Resultado(false, "Via não encontrada no grafo.", null);
        }
        if (aresta.estaBloqueada()) {
            return new Resultado(false, "Via já está bloqueada.", aresta);
        }

        aresta.setStatus(StatusVia.BLOQUEADA);
        return new Resultado(true,
                "Via '" + aresta.getOrigem().getNome() + "' → '" + aresta.getDestino().getNome()
                        + "' bloqueada. Peso efetivo: ∞", aresta);
    }

    /**
     * Marca uma via como congestionada (RN04).
     * O peso efetivo é multiplicado por 1.5.
     *
     * @param grafo  o grafo da cidade
     * @param aresta a aresta a ser congestionada
     * @return Resultado da operação
     */
    public static Resultado congestionarVia(GrafoCidade grafo, Aresta aresta) {
        if (grafo == null || aresta == null) {
            return new Resultado(false, "Parâmetros inválidos.", null);
        }
        if (!grafo.getArestas().contains(aresta)) {
            return new Resultado(false, "Via não encontrada no grafo.", null);
        }
        if (aresta.estaBloqueada()) {
            return new Resultado(false, "Via está bloqueada. Desbloqueie antes de congestionar.", aresta);
        }
        if (aresta.estaCongestionada()) {
            return new Resultado(false, "Via já está congestionada.", aresta);
        }

        aresta.setStatus(StatusVia.CONGESTIONADA);
        return new Resultado(true,
                "Via '" + aresta.getOrigem().getNome() + "' → '" + aresta.getDestino().getNome()
                        + "' congestionada. Peso efetivo: " + aresta.getPesoEfetivo() + " min.", aresta);
    }

    /**
     * Libera uma via bloqueada ou congestionada (RF09).
     * O peso volta ao valor original sem multiplicador.
     *
     * @param grafo  o grafo da cidade
     * @param aresta a aresta a ser liberada
     * @return Resultado da operação
     */
    public static Resultado liberarVia(GrafoCidade grafo, Aresta aresta) {
        if (grafo == null || aresta == null) {
            return new Resultado(false, "Parâmetros inválidos.", null);
        }
        if (!grafo.getArestas().contains(aresta)) {
            return new Resultado(false, "Via não encontrada no grafo.", null);
        }
        if (aresta.estaLivre()) {
            return new Resultado(false, "Via já está livre.", aresta);
        }

        aresta.setStatus(StatusVia.LIVRE);
        return new Resultado(true,
                "Via '" + aresta.getOrigem().getNome() + "' → '" + aresta.getDestino().getNome()
                        + "' liberada. Peso efetivo: " + aresta.getPesoEfetivo() + " min.", aresta);
    }

    /**
     * Atualiza o peso base de uma via (tempo de deslocamento em minutos).
     *
     * @param grafo     o grafo da cidade
     * @param aresta    a aresta a ser atualizada
     * @param novoPeso  novo peso base em minutos (deve ser positivo)
     * @return Resultado da operação
     */
    public static Resultado atualizarPeso(GrafoCidade grafo, Aresta aresta, double novoPeso) {
        if (grafo == null || aresta == null) {
            return new Resultado(false, "Parâmetros inválidos.", null);
        }
        if (!grafo.getArestas().contains(aresta)) {
            return new Resultado(false, "Via não encontrada no grafo.", null);
        }
        if (novoPeso <= 0) {
            return new Resultado(false, "Peso deve ser maior que zero.", aresta);
        }

        double pesoAnterior = aresta.getPeso();
        aresta.setPeso(novoPeso);
        return new Resultado(true,
                "Peso da via '" + aresta.getOrigem().getNome() + "' → '" + aresta.getDestino().getNome()
                        + "' atualizado de " + pesoAnterior + " para " + novoPeso + " min.", aresta);
    }

    /**
     * Retorna todas as vias bloqueadas do grafo.
     *
     * @param grafo o grafo da cidade
     * @return lista de arestas bloqueadas
     */
    public static List<Aresta> getViasBloqueadas(GrafoCidade grafo) {
        if (grafo == null) return Collections.emptyList();
        List<Aresta> bloqueadas = new ArrayList<>();
        for (Aresta a : grafo.getArestas()) {
            if (a.estaBloqueada()) bloqueadas.add(a);
        }
        return bloqueadas;
    }

    /**
     * Retorna todas as vias congestionadas do grafo.
     *
     * @param grafo o grafo da cidade
     * @return lista de arestas congestionadas
     */
    public static List<Aresta> getViasCongestionadas(GrafoCidade grafo) {
        if (grafo == null) return Collections.emptyList();
        List<Aresta> congestionadas = new ArrayList<>();
        for (Aresta a : grafo.getArestas()) {
            if (a.estaCongestionada()) congestionadas.add(a);
        }
        return congestionadas;
    }
}
