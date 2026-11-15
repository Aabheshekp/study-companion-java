# Study Companion (Java)

An offline Java desktop study system with decks, searchable flashcards, due queues, and a lightweight spaced-repetition scheduler. The UI uses Swing and the JDK only; card data is stored locally as an atomic UTF-8 TSV file.

## Requirements and launch
Java 17+ and Maven 3.9+.
```powershell
mvn package
mvn exec:java
```
Set `STUDY_COMPANION_DATA` to choose the data file. A small starter deck is created on first run. No account, network service, API key, or telemetry is used.

## Features
- Create, edit, search, and delete cards; organize them by deck.
- Review due cards with Again (retry tomorrow), Hard, and Good ratings.
- Scheduler stores interval, repetitions, ease, and due date per card.
- Daily due/overdue counts and deck-level review statistics.
- Import/export UTF-8 CSV using the File menu.

See [design notes](docs/design.md). MIT license.
