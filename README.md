# 🃏 El Cincuentazo

> Juego de cartas multijugador desarrollado en Java con JavaFX, donde la suma acumulada en la mesa no puede superar **50**. El último jugador en pie gana.

---

## 📋 Tabla de Contenidos

- [Sobre el juego](#-sobre-el-juego)
- [Reglas](#-reglas)
- [Tecnologías](#-tecnologías)
- [Arquitectura del proyecto](#-arquitectura-del-proyecto)
- [Estructura de paquetes](#-estructura-de-paquetes)
- [Clases principales](#-clases-principales)
- [Patrones de diseño](#-patrones-de-diseño)
- [Requisitos](#-requisitos)
- [Cómo ejecutar](#-cómo-ejecutar)
- [Tests](#-tests)
- [Autores](#-autores)

---

## 🎮 Sobre el juego

**El Cincuentazo** es un juego de cartas para 2 a 4 jugadores (1 humano y hasta 3 rivales controlados por IA). Cada jugador mantiene una mano de 4 cartas en todo momento. En cada turno, el jugador debe colocar una carta en la mesa sin que la suma total supere 50. Si no puede hacerlo, queda eliminado. El último jugador activo gana la partida.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![JUnit](https://img.shields.io/badge/JUnit-5-green?style=flat-square&logo=junit5)
![MVC](https://img.shields.io/badge/Patrón-MVC-purple?style=flat-square)

---

## 📖 Reglas

| Carta | Efecto en la suma |
|-------|------------------|
| 2 – 8, 10 | Suma su valor numérico |
| 9 | Neutral (no suma ni resta) |
| J, Q, K | Resta 10 |
| As (A) | Suma 10 si no supera 50, de lo contrario suma 1 |

- Cada jugador comienza con **4 cartas** en mano.
- Tras jugar una carta, el jugador **roba automáticamente** una del mazo para mantener siempre 4 cartas.
- Si el mazo se agota, las cartas de la mesa (excepto la última jugada) se **barajan y reciclan**.
- Si un jugador no puede jugar **ninguna** carta sin superar 50, queda **eliminado**.
- Gana el **último jugador** que permanezca en la partida.

---

## 🛠️ Tecnologías

- **Java 21** — Lenguaje principal
- **JavaFX 21** — Interfaz gráfica y FXML
- **JUnit 5** — Pruebas unitarias
- **CSS** — Estilos visuales de la interfaz (tema casino)
- **Maven** — Gestión de dependencias y build

---

## 🏗️ Arquitectura del proyecto

El proyecto implementa el patrón **MVC (Modelo – Vista – Controlador)** combinado con el patrón **Observador**, logrando una separación clara de responsabilidades entre capas.

```
┌─────────────────────────────────────────────────┐
│                     VISTA                        │
│         MainMenuView / GameScreenView            │
│              (carga FXML, muestra UI)            │
└───────────────────┬─────────────────────────────┘
                    │ inyecta modelo via initModel()
┌───────────────────▼─────────────────────────────┐
│                 CONTROLADOR                      │
│   MainMenuController / GameScreenController      │
│         (traduce eventos GUI → modelo)           │
└───────────────────┬─────────────────────────────┘
                    │ llama a métodos del modelo
┌───────────────────▼─────────────────────────────┐
│                   MODELO                         │
│    GameLogic / TurnManager / Deck / Player       │
│         (lógica de negocio pura)                 │
│                    │                             │
│        fires GameEventListener callbacks         │
└─────────────────────────────────────────────────┘
```

---

## 📁 Estructura de paquetes

```
src/
└── main/
    ├── java/com/example/elcincuentazo/
    │   ├── main.java                        # Punto de entrada de la aplicación
    │   │
    │   ├── model/                           # Lógica de negocio
    │   │   ├── GameLogic.java               # Motor central del juego
    │   │   ├── TurnManager.java             # Gestión de turnos y concurrencia
    │   │   ├── Deck.java                    # Mazo de cartas
    │   │   ├── Card.java                    # Representación de una carta
    │   │   ├── Player.java                  # Clase abstracta de jugador
    │   │   ├── HumanPlayer.java             # Jugador humano
    │   │   ├── MachinePlayer.java           # Jugador IA
    │   │   ├── PlayerHand.java              # Mano de un jugador
    │   │   ├── CardValueCalculator.java     # Cálculo del valor de cartas
    │   │   └── GameState.java              # Snapshot inmutable del estado
    │   │
    │   ├── view/                            # Carga de pantallas FXML
    │   │   ├── MainMenuView.java
    │   │   └── GameScreenView.java
    │   │
    │   ├── controller/                      # Controladores de la UI
    │   │   ├── MainMenuController.java
    │   │   ├── GameScreenController.java
    │   │   └── GameController.java
    │   │
    │   ├── observer/                        # Patrón Observador
    │   │   └── GameEventListener.java
    │   │
    │   └── exceptions/                      # Excepciones personalizadas
    │       ├── InvalidCardPlayException.java
    │       ├── GameOverException.java
    │       └── EmptyDeckException.java
    │
    └── resources/com/example/elcincuentazo/
        ├── MainMenu.fxml                    # UI del menú principal
        ├── GameScreen.fxml                  # UI de la pantalla de juego
        └── styles.css                       # Estilos visuales (tema casino)
```

---

## 🔑 Clases principales

### `GameLogic`
Motor central del juego. Orquesta todas las fases de la partida.

| Método | Descripción |
|--------|-------------|
| `setupGame(machineCount)` | Inicializa la partida: crea el mazo, los jugadores y reparte cartas |
| `humanPlayCard(card)` | Valida y ejecuta la jugada del jugador humano |
| `executeMachinePlay()` | Ejecuta el turno completo de una máquina |
| `checkAndEliminateCurrentPlayer()` | Verifica si el jugador en turno debe ser eliminado |
| `advanceTurn()` | Avanza al siguiente jugador activo en orden circular |
| `isGameOver()` | Retorna si la partida ha finalizado |

---

### `CardValueCalculator`
Clase utilitaria estática que aplica las reglas de valor de cada carta.

| Método | Descripción |
|--------|-------------|
| `calculateDelta(card, currentSum)` | Calcula cuánto suma o resta una carta |
| `isPlayable(card, currentSum)` | Indica si una carta puede jugarse sin superar 50 |
| `aceOptimalDelta(currentSum)` | Determina si el As debe valer 1 o 10 |
| `describeEffect(card, currentSum)` | Descripción legible del efecto de la carta |

---

### `Deck`
Gestiona el mazo de robo y la pila de mesa.

| Método | Descripción |
|--------|-------------|
| `drawCard()` | Roba la carta superior del mazo |
| `placeOnTable(card)` | Coloca una carta en la mesa |
| `refillFromTablePile()` | Recicla la mesa cuando el mazo se agota *(privado)* |
| `peekTableTop()` | Consulta la última carta jugada sin retirarla |
| `returnCardsToBottom(cards)` | Devuelve cartas de jugadores eliminados al mazo |

---

### `TurnManager`
Gestiona la concurrencia y el ritmo de los turnos de las máquinas.

| Método | Descripción |
|--------|-------------|
| `start()` | Inicia el ciclo de turnos |
| `stop()` | Detiene el scheduler y cancela tareas pendientes |
| `onHumanTurnCompleted()` | Notifica que el humano terminó su turno |
| `triggerNextTurnIfMachine()` | Programa el turno de la siguiente máquina si corresponde |

---

### `MachinePlayer`
Jugador controlado por IA con estrategia greedy.

| Método | Descripción |
|--------|-------------|
| `chooseBestCard(currentSum)` | Elige la carta que maximiza la presión sobre los rivales |
| `getPlayableCards(currentSum)` | Retorna todas las cartas legalmente jugables |

---

## 🎨 Patrones de diseño

### MVC (Modelo – Vista – Controlador)
- El **modelo** (`GameLogic`, `Deck`, `Player`, etc.) no tiene ninguna dependencia de JavaFX.
- La **vista** (`MainMenuView`, `GameScreenView`) solo carga FXML e inyecta el modelo.
- El **controlador** (`GameScreenController`) traduce eventos de la UI en llamadas al modelo.

### Observer (Patrón Observador)
La interfaz `GameEventListener` define los siguientes callbacks que el modelo dispara automáticamente:

| Evento | Cuándo se dispara |
|--------|--------------------|
| `onGameStarted(tableSum)` | Al terminar la configuración inicial |
| `onTurnStarted(player)` | Al inicio del turno de cada jugador |
| `onCardPlayed(player, card, newSum)` | Tras una jugada exitosa |
| `onCardDrawn(player, card)` | Al robar una carta de reemplazo |
| `onPlayerEliminated(player)` | Cuando un jugador no puede jugar |
| `onGameOver(winner)` | Cuando queda un solo jugador activo |
| `onInvalidPlay(player, card, sum)` | Si el humano intenta una jugada ilegal |

---

## ⚙️ Requisitos

- **Java 21** o superior
- **JavaFX 21** (incluido en las dependencias del proyecto)
- **Maven 3.8+**

---

## 🚀 Cómo ejecutar

### Clonar el repositorio
```bash
git clone https://github.com/tu-usuario/el-cincuentazo.git
cd el-cincuentazo
```

### Compilar y ejecutar con Maven
```bash
mvn clean javafx:run
```

### Compilar solamente
```bash
mvn clean compile
```

---

## 🧪 Tests

El proyecto incluye pruebas unitarias con **JUnit 5** para las clases más críticas del modelo.

```bash
mvn test
```

### Cobertura de pruebas

| Clase de Test | Qué valida |
|---------------|------------|
| `CardValueCalculatorTest` | Reglas de valor para cada tipo de carta, casos borde del As y la barrera de 50 |
| `DeckTest` | Tamaño inicial, unicidad de cartas, robo, reciclaje automático y devolución de cartas |
| `GameLogicTest` | Flujo completo de partida, eliminaciones, excepciones y disparo correcto de eventos |

---

## 👥 Autores

Desarrollado como **Mini-Proyecto 3** por el equipo:

| Nombre | Código |
|--------|--------|
| Juan Sebastián Duarte Quintero | 2516473 |
| Santiago Torres Martínez | 2521423 |
| Wilson Pinto Córdoba | 2521251 |

---

## 📄 Licencia

Este proyecto fue desarrollado con fines académicos.
