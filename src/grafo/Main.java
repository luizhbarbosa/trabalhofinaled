package grafo;

import telas.TelaPrincipal;

/**
 * Ponto de entrada do sistema de emergencia SAMU.
 * Apenas abre a interface gráfica. A simulação é iniciada
 * pelo usuário clicando em "Iniciar Simulação".
 */
public class Main {

    public static void main(String[] args) {
        // Apenas abre a tela principal - sem logs prévios, sem população automática
        new TelaPrincipal();
    }
}