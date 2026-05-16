# Staircase Card Game

A desktop card game developed with Kotlin as part of a university software project.

The game is based on a two-player card staircase concept. Players take turns playing, drawing, discarding, combining, destroying, and collecting cards. The goal is to collect valuable cards and achieve a higher score than the opponent.

## Features

- Two-player card game
- 52-card deck
- Staircase layout with 15 cards
- Player hands, draw stack, discard stack, and collected cards
- Turn-based gameplay
- Graphical user interface
- Game result screen
- Unit tests for important game logic

## How to Play

The game is played by two players. At the beginning of the game, each player receives a hand of cards, and a staircase of cards is placed on the table.

The staircase contains 15 cards arranged in five rows:

```text
1 + 2 + 3 + 4 + 5 = 15 cards
```

Players take turns and try to collect valuable cards from the staircase.

During a turn, a player can perform actions such as:

- playing or combining cards from their hand (combine by same value or same suit)
- collecting cards from the staircase
- discarding a card
- drawing a new card
- destroying cards depending on the game situation

Collected cards are added to the player's score.

The game continues until the end condition is reached (Stair case is empty). After that, the result screen shows the final scores and the winner.

The goal of the game is to collect more points than the opponent.

## Project Structure

```text
src/main/kotlin/
├── entity/      # Game data classes and core models
├── service/     # Game logic and service layer
├── gui/         # Graphical user interface
└── Main.kt      # Application entry point

src/test/kotlin/
├── entity/      # Tests for entity classes
└── service/     # Tests for service logic
```

## Technologies Used

- Kotlin
- Gradle
- JavaFX / BoardGameWork GUI framework
- JUnit tests
- Detekt for code quality checks

## How to Run the Game

The easiest way to run the game is to download the ready-to-run version from the GitHub Releases section.

### Run on Windows

1. Go to the **Releases** section of this repository.
2. Download `distribution.zip`.
3. Extract the ZIP file.
4. Open the extracted folder.
5. Go to:

```text
bin/
```

6. Double-click:

```text
projekt1.bat
```

The game should start directly.

### Run on macOS/Linux

1. Download `distribution.zip` from the **Releases** section.
2. Extract the ZIP file.
3. Open a terminal inside the extracted folder.
4. Run:

```bash
./bin/projekt1
```

If needed, make the file executable first:

```bash
chmod +x bin/projekt1
```

## Building from Source

This project was developed as part of a university software project.

Some dependencies may be hosted in a private university GitLab package registry. Because of that, building the project from source may require valid university package registry access.

For reviewers, the recommended way to run the game is to use the ready-to-run `distribution.zip` from the Releases section.

## Tests

The project contains unit tests for core parts of the game logic.


## Notes

This project focuses on clean separation between:

- entity layer
- service layer
- graphical user interface layer

The goal was to implement a playable desktop card game while following object-oriented design principles and testing important service logic.

## About the Project

This project was created during a university software project. It helped me practice Kotlin, object-oriented programming, service-based architecture, GUI development, and unit testing in a larger project structure.