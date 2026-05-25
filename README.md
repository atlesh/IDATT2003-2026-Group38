# Millions

Millions is a JavaFX-based stock trading simulation developed as a mappeprosjekt in IDATT2003 at NTNU.

The player starts with a fixed amount of money and trades stocks across weekly market updates. The goal is to grow net worth through buying and selling shares while tracking portfolio performance, transaction history, and market movements.

## Group Members

- Sigurd Mjølstad-Svendsen
- Atle Sandnes Halvorsen

## Features

- Start a new game
- Load a previously saved game
- Manual save and autosave
- Weekly market progression
- Buy and sell shares
- Sell all holdings
- Portfolio overview and transaction history
- Dashboard with net worth development
- End-game summary
- Random market events and rumors
- Detailed stock analysis from both Market and Portfolio views

## Technologies and Architecture

The project is built with:

- Java 25
- JavaFX
- Maven
- JUnit 5
- Jackson for JSON save/load

The application follows an MVC-inspired structure with separate packages for:

- `model`
- `view`
- `controller`
- `exchange`
- `transaction`
- `filehandling`
- `event`

The project also uses observer-based updates between the model and UI where appropriate.

## Running the Project

This project requires Java 25.

To run the application:

```bash
mvn javafx:run
```

If needed, compile first:

```bash
mvn compile
```

## Running Tests

To run all tests:

```bash
mvn test
```

## Save Files

Game saves are stored as JSON files.

- Manual saves can be created from the in-game shell
- Autosave is written automatically when advancing to the next week
- Save files are stored in the `saves/` directory

## Project Structure

- `src/main/java/no/ntnu/idatt2003/group38`
- `src/test/java/no/ntnu/idatt2003/group38`
- `src/main/resources`
