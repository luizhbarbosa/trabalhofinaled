# SAMU — Mapeamento de Rotas de Ambulâncias
### Overview do projeto para apresentação / onboarding

---

## 1. O que é o projeto

Modelagem de um **sistema de despacho de ambulâncias do SAMU** como um **grafo dirigido e ponderado**. A cidade vira um grafo: hospitais, bases SAMU, bairros, cruzamentos e pacientes são **vértices**; ruas são **arestas** com peso dinâmico (tempo de deslocamento), que muda conforme o trânsito.

O projeto é acadêmico e tem **4 entregas obrigatórias**:
1. Backend em Java (grafo + algoritmos + regras de negócio)
2. Interface gráfica em Swing
3. Artigo em LaTeX
4. Slides em Beamer

**Objetivo didático:** aplicar teoria de grafos (Dijkstra, A*, BFS, Componentes Conexos) num problema do mundo real com regras de negócio de verdade.

---

## 2. Modelagem do domínio

```
Vertice (classe base)
 ├── Hospital     (capacidade, ocupação atual, disponibilidade)
 ├── Paciente     (nível de urgência, descrição da ocorrência)
 └── (Base SAMU, Bairro, Cruzamento — vértices genéricos sem subclasse própria)

Aresta
 └── origem, destino, peso (minutos), status (LIVRE / CONGESTIONADA / BLOQUEADA)
```

- **`GrafoCidade`**: estrutura de dados central. Guarda vértices, arestas, e um **mapa de adjacência** (`Map<Integer, List<Aresta>>`) pra consultas rápidas de "quais ruas saem deste ponto".
- **`Ambulancia`**: tem localização atual e status (`DISPONIVEL` / `EM_ATENDIMENTO`).
- **`SistemaEmergencia`**: o orquestrador — é a classe que qualquer novo desenvolvedor vai mexer com mais frequência, porque é o ponto de entrada de todo o fluxo de negócio.

Terminologia do domínio é toda em português (`registrarOcorrencia`, `localizarAmbulanciaProxima`, etc.), reflexo direto dos requisitos funcionais (RF01–RF12) e regras de negócio (RN01–RN05) da especificação oficial.

---

## 3. Os 4 algoritmos e pra que cada um serve

| Algoritmo | Onde é usado | Por quê |
|---|---|---|
| **Dijkstra** | Rota ambulância → paciente | Menor custo/tempo real, considerando vias bloqueadas/congestionadas |
| **A\*** | Rota paciente → hospital mais próximo | Mesma ideia do Dijkstra, mas guiado por heurística de distância euclidiana (mais eficiente quando o destino é conhecido) |
| **BFS** | Análise estrutural da malha viária | Menor número de cruzamentos (não de tempo) — usado pra conectividade |
| **Componentes Conexos** | Análise de cobertura (RF12) | Detecta bairros isolados sem acesso a hospital |

Os quatro algoritmos vivem em classes próprias (`Dijkstra`, `AEstrela`, `BFS`, `ComponentesConexos`), cada uma com uma classe interna `Resultado` que carrega o caminho encontrado + custo/métrica.

---

## 4. O fluxo de uma emergência, ponta a ponta

Esse é o coração do sistema — o que acontece quando uma ocorrência é registrada:

```
1. registrarOcorrencia(paciente)
       │
       ├─ adiciona paciente como vértice no grafo
       ├─ conecta paciente ao vértice mais próximo automaticamente
       │  (conectarPacienteAoVerticeMaisProximo)
       │
       └─ dispara atenderNovaOcorrencia(paciente):
              │
              ├─ localizarAmbulanciaProxima()      → Dijkstra
              ├─ calcularRotaAmbulanciaParaPaciente() → Dijkstra
              └─ selecionarHospitalDestino()        → A*
```

Tudo isso acontece **numa única chamada** a `registrarOcorrencia()`, que retorna um `AtendimentoResultado` com ambulância, rota e hospital já resolvidos. Essa é uma decisão de design importante: a regra de negócio **RN05** ("toda nova ocorrência deve gerar uma nova análise de rota") é garantida **pela própria estrutura do código**, não por convenção de quem chama o sistema.

Depois disso, o fluxo segue com:
- `despacharAmbulancia()` — muda status pra `EM_ATENDIMENTO`
- `atualizarStatusVia()` / `GerenciadorVias` — bloqueios e congestionamentos em tempo real
- `RecalculoRota` — detecta se um bloqueio afeta a rota em curso e recalcula automaticamente
- `finalizarAtendimento()` — libera a ambulância

---

## 5. Regras de negócio (RN01–RN05)

| Regra | O que garante |
|---|---|
| RN01 | Ambulância só pode atender uma ocorrência por vez |
| RN02 | Hospitais lotados são ignorados na seleção de destino |
| RN03 | Vias bloqueadas disparam recálculo automático de rota |
| RN04 | Vias congestionadas têm peso aumentado (multiplicador 1.5x) |
| RN05 | Toda ocorrência nova gera análise de rota completa (garantida por construção, ver seção 4) |

---

## 6. Decisões de design que valem explicar na apresentação

- **`equals()` por `id`, usando `instanceof`** em vez de `getClass()`: permite que `Hospital` e `Paciente` (subclasses de `Vertice`) sejam corretamente comparados/localizados em `HashMap`/`HashSet` junto com vértices genéricos.
- **Bloqueio de via = peso infinito, não remoção de aresta**: mantém a topologia do grafo intacta e permite reverter o bloqueio instantaneamente, sem reconstruir a aresta. Diverge da spec original (que sugere remoção), mas é uma escolha tecnicamente equivalente em complexidade.
- **Dijkstra/A\* usam a lista de adjacência do grafo** (`getArestasSaida(vertice)`), não uma varredura de todas as arestas — isso é o que garante a complexidade O(E log V) exigida pela RNF04 (desempenho em redes urbanas de grande porte), em vez de O(V×E).
- **Conexão do paciente ao grafo é automática e por proximidade real** (`calcularDistancia()`), não hardcoded — importante porque a futura GUI vai permitir clicar em qualquer ponto do mapa, e o paciente pode aparecer em qualquer lugar.

---

## 7. Estado atual do projeto

✅ **Completo e testado:**
- Modelagem do grafo (vértices, arestas, adjacência)
- Os 4 algoritmos, com complexidade corrigida
- Regras de negócio RN01–RN05
- Fluxo de ponta a ponta rodando via `Main.java`

🔴 **Ainda faltando (por ordem de prioridade sugerida):**
1. **Interface gráfica Swing** — maior bloco de trabalho pendente. 4 telas: `TelaPrincipal`, `TelaCadastroHospital`, `TelaCadastroVia`, `TelaAtendimento`
2. **Classe `AnaliseCobertura` dedicada** — hoje é só um método dentro de `SistemaEmergencia`; falta também o cálculo de distância média entre bairros e hospitais
3. **Testes automatizados (JUnit)** — hoje a única "cobertura de teste" é o `Main.java` rodando manualmente
4. **Artigo LaTeX + Slides Beamer** — documentação acadêmica

---

## 8. Stack e ambiente de desenvolvimento

- **Linguagem:** Java
- **GUI (a fazer):** Swing
- **Build:** Ant (estrutura compatível com NetBeans, `nbproject/` + `build.xml`)
- **IDEs:** VS Code pra lógica de backend, NetBeans pra interface Swing
- **⚠️ Atenção pra quem for compilar no Windows:** o projeto usa acentuação (português) nos textos. Sempre compilar com `-encoding UTF-8` e rodar `chcp 65001` no terminal antes de executar, ou os acentos saem corrompidos (`nÃ£o`, `Ã¡` etc.) — isso é só exibição no console, não afeta o funcionamento.

---


