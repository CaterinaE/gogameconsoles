 # How to Play Go: A Game Guide

## Objective:
The goal of Go is to control more territory than your opponent by strategically placing stones on the board.

## Board:
- Go is typically played on a 19x19 grid, beginners can start on smaller boards like 9x9 or 13x13 to learn the basics.
- 9x9 board is being used for this Go game.
- Players take turns placing their stones on the intersections of the lines.

## Stones:
- There are two types of stones: black and white.
- Black plays first, and then turns alternate. Black is O and White is X for the console input.

## Gameplay:
### Opening:
- The game usually starts with an empty board. Players take turns placing one stone at a time on any unoccupied intersection.

### Capturing:
- When a stone or group of stones becomes surrounded by the opponent's stones, it is captured and removed from the board. Stones are captured when they no longer have any liberties (empty adjacent intersections).

### Liberties:
- A single stone or a group of stones must have at least one liberty to remain on the board. Liberties are empty adjacent intersections. A group of stones shares its liberties.

### Ko Rule:
- The Ko rule is in effect to prevent an endless repetition of moves. If a move creates the same board position as the previous turn, it is illegal.

### Passing:
- Instead of placing a stone on their turn, a player may choose to pass. If both players pass consecutively, the game ends, and territory is counted.

## Capturing Stones:
- When a stone or group of stones is entirely surrounded by the opponent's stones, it is captured and removed from the board.

## Connecting Stones:
- To prevent your stones from getting captured, you should try to connect them. Connecting stones form a group, and as long as the group has at least one liberty (empty adjacent intersection), it remains on the board.

## Creating Liberties:
- During the game, players extend their influence by creating groups with multiple liberties. More liberties make it harder for the opponent to capture those stones.

## Capturing Races:
- Sometimes, capturing stones can lead to tactical races where players attempt to capture each other's stones in a sequence of moves. These situations can be quite exciting and require careful calculation.

## Sacrifice Stones:
- In some cases, sacrificing a stone or a group of stones strategically can help you gain a better position elsewhere on the board.

## Capturing Stones (Examples):
- If one stone is surrounded by the opponent's stones, it is captured and removed from the board.
- If a group of stones is surrounded with one liberty, it is captured.

## How to Play the Game:
1. Run the `play()` method in your main program to start the game.
2. The menu will be displayed, and the user can make a choice from 1 to 4.
3. Based on the user's choice, the game will be initialized.
4. The user can make moves using the specified format for row and column inputs.
5. The AI helper can be used by entering 'a' during the user's turn.