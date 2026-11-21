# 🐾 Animal Tic Tac Toe – Neon Animal Battle

A futuristic neon-themed Tic Tac Toe Android game where animals battle instead of traditional X and O symbols!

**Animal Tic Tac Toe** brings a fresh twist to the classic Tic Tac Toe game with beautiful animal characters, glowing neon graphics, AI opponents, multiplayer gameplay and multiple board sizes.

## 🎮 Game Preview

<!-- Add your game screenshots here -->

| Home Screen | Animal Selection | Gameplay |
|---|---|---|
| Add Screenshot | Add Screenshot | Add Screenshot |

## ✨ Features

- 🦁 **Animal Characters:** Replace traditional X and O with animal symbols.
- 🤖 **Play vs AI:** Challenge computer opponents.
- 👥 **Two Player Mode:** Play with friends on the same device.
- 🎯 **Multiple Board Sizes:** 3×3, 5×5, 7×7 and 9×9.
- 🧠 **AI Difficulty:** Easy, Medium, Hard and Expert.
- 🏆 **Statistics:** Track wins, losses, draws and winning streaks.
- 🎨 **Neon UI:** Futuristic cyber arcade design.
- ⚡ **Animations:** Glowing symbols, winning effects and smooth transitions.
- 🔊 **Sound Effects:** Game sounds and interactive feedback.
- 📳 **Haptic Feedback:** Vibration during gameplay.
- 📱 **Offline Gameplay:** Play without an internet connection.
- ⚙️ **Settings:** Customize your gaming experience.

## 🐺 Animal Characters

The game uses animal characters instead of traditional Tic Tac Toe symbols.

| Animal | Player Theme |
|---|---|
| 🦁 Lion | Electric Cyan |
| 🐺 Wolf | Golden Amber |
| 🐯 Tiger | Custom Theme |
| 🦅 Eagle | Custom Theme |
| 🦊 Fox | Custom Theme |
| 🐼 Panda | Custom Theme |
| 🐻 Bear | Custom Theme |
| 🐉 Dragon | Custom Theme |

*Additional characters can be introduced through future updates.*

## 🎯 Game Modes

### 1. Player vs AI
Play against an intelligent computer opponent with different difficulty levels.

### 2. Two Player
Enjoy local multiplayer gameplay with two players sharing one device.

### 3. Classic Game
Play the traditional 3×3 Tic Tac Toe experience using animal symbols.

### 4. Large Board Challenge
Challenge yourself with larger 5×5, 7×7 and 9×9 boards.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Programming Language |
| Jetpack Compose | Android UI |
| Android Studio | Development Environment |
| MVVM | Application Architecture |
| Kotlin Coroutines | Asynchronous Operations |
| StateFlow | Reactive State Management |
| DataStore | Local Preferences |
| Canvas | Custom Graphics |
| Gradle | Build Management |

## 📂 Project Structure

```text
AnimalTicTacToe/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/example/animaltictactoe/
│           │       │
│           │       ├── data/
│           │       │   ├── repository/
│           │       │   └── preferences/
│           │       │
│           │       ├── domain/
│           │       │   ├── GameEngine.kt
│           │       │   ├── GameState.kt
│           │       │   ├── AIEngine.kt
│           │       │   └── GameMode.kt
│           │       │
│           │       ├── ui
