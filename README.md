# Chess Engine Library (Kotlin) 🧠♟️

A comprehensive chess engine implementation in Kotlin that supports all standard chess rules and special moves.

## Features

- <b>Complete chess rule implementation</b>
- <b>Special moves support:</b>
  - Castling (Kingside and Queenside)
  - En Passant
  - Pawn Promotion
- <b>Advanced chess mechanics:</b>
  - Fork detection
  - Valid move calculation
  - Checkmate verification
  - Stalemate detection
- <b>Game state management:</b>
  - Current player tracking
  - Move history
  - Undo functionality
  - Board reset

## Installation
Add the dependency to your project:

- Step 1: Add in `settings.gradle.kts`

```gradle
dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
  }
}
```
- Step 2: Add in `build.gradle.kts`
```.kts
dependencies {
  implementation("com.github.spongycode:chess-engine:v1.0.1") // change with latest-version
}
```

## Usage Example 🎮
For detailed examples, refer to the sample project included in the repository.
```kotlin
// Initialize the ChessEngine  
val chessEngine = ChessEngine()  

// Get the current player's turn  
val currentPlayer = chessEngine.getCurrentPlayer()  

// Make a move  
chessEngine.makeMove("e2", "e4")  

// Make a move for pawn promotion  
chessEngine.makeMove("a7", "a8Q") // 'Q' denotes promotion to a queen.  

// Get possible moves for a piece  
val moves = chessEngine.getMoves("e2")  // Example: ["e3", "e4"]  

// Get possible moves for a piece with pawn promotion  
val promotionMoves = chessEngine.getMoves("b7")  // Example: ["b8+", "c8+"] ('+' denotes pawn promotion).  

// Undo the last move  
chessEngine.undo()  

// Reset the game to its initial state  
chessEngine.reset()  

// Check if there is a winner  
val winner = chessEngine.getWinner()  
```

<b>Command Line interaction</b>

```kotlin
fun main() {
  val chessBoard = ChessBoard()
  val board = chessBoard.getBoard()
  
  val chessGame = ChessGame(chessBoard = board)
  
  while (chessGame.getWinner() == null) {
    chessGame.printBoard()
    try {
      val input = readln().split(" ")
      if (input.size == 2) {
        val start = input[0]
        val end = input[1]
        chessGame.makeMove(start, end)
      } else {
        val start = input[0]
        val moves = chessGame.getMoves(start)
        println(moves)
      }
    } catch (e: Exception) {
      println(e)
    }
  }
  println("WINNER: ${chessGame.getWinner()}")
}
/**
 *  Example Output:
 *  
 *  8  BR BN BB BQ BK BB BN BR  
 *  7  BP BP BP BP BP BP BP BP  
 *  6                         
 *  5                         
 *  4                         
 *  3                         
 *  2  WP WP WP WP WP WP WP WP  
 *  1  WR WN WB WQ WK WB WN WR  
 *      A  B  C  D  E  F  G  H  
 *  
 *  e2 e4  
 *  
 *  8  BR BN BB BQ BK BB BN BR  
 *  7  BP BP BP BP BP BP BP BP  
 *  6                         
 *  5                         
 *  4              WP          
 *  3                         
 *  2  WP WP WP WP    WP WP WP  
 *  1  WR WN WB WQ WK WB WN WR  
 *      A  B  C  D  E  F  G  H  
 *  
 *  c7
 *
 *  [c5, c6]  
 *
 *  and so on...  
 */
```

## Contributing
Feel free to contribute to this project by submitting issues, pull requests, or providing valuable feedback. Your contributions are always welcome! 🙌

## License 📄
Chess Engine is released under the [MIT License](https://opensource.org/licenses/MIT). Feel free to modify or add to this list based on the specific features of your app.

## Happy coding! 🎉👩‍💻👨‍💻
