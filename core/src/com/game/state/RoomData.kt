package com.game.state

import com.game.entity.*
import com.game.maths.Direction
import com.game.maths.RandomUtils
import com.game.maths.Tile
import tilemap.TileMap
import java.util.*

class RoomData(val difficulty: Int,
               val north: Boolean, val east: Boolean, val south: Boolean, val west: Boolean,
               val objects: Array<Pair<Char, Tile>>) {

    val tiles: TileMap = TileMap(32, 20, 24, "tiles", 5)
    val enemyMap: Array<String> = newEnemyMap()
    var cleared: Boolean = false

    init {
        for(i in 0 until tiles.width) {
            for(j in 0..1) {
                tiles.tiles[i][j] = 5
                tiles.tiles[i][tiles.height - 1 - j] = 5
            }
        }

        for(i in 0 until tiles.height) {
            for(j in 0..7) {
                tiles.tiles[j][i] = 5
                tiles.tiles[tiles.width - 1 - j][i] = 5
            }
        }
    }


    fun makeRoom(enteredFrom: Direction): RoomState {

        val playerPos = when(enteredFrom.opposite()) {
            Direction.NORTH -> Tile(15, 17)
            Direction.SOUTH -> Tile(15, 2)
            Direction.EAST -> Tile(23, 10)
            else -> Tile(8, 10)
        }

        val room = RoomState(playerPos, north, east, south, west, tiles)

        var enemies = 0

        objects.asSequence().forEach {
            when {
                it.first == 'w' -> tiles.tiles[it.second.x + 8][it.second.y + 2] = 5

                it.first.isDigit() -> {
                    val group: Int = intValidOf(it.first)
                    val newEnemy: Enemy = makeEnemy(room, it.second, group, enemies)
                    room.entities.add(newEnemy)
                    ++enemies
                }

            }
        }

        if(enemies > 0) {
            room.closeDoors()
        } else {
            room.openDoors()
        }

        return room
    }


    private fun newEnemyMap(): Array<String> {
        val mutable: MutableList<String> = when(difficulty) {
            0 -> mutableListOf(
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "gold_skeleton",
                    "slime",
                    "slime",
                    "vampire",
                    "vampire",
                    "dark_knight"
            )

            1 -> mutableListOf(
                    "black_skeleton",
                    "skeleton",
                    "skeleton",
                    "gold_skeleton",
                    "wisp",
                    "wisp",
                    "vampire",
                    "vampire",
                    "dark_knight",
                    "dark_knight"
            )

            2 -> mutableListOf(
                    "black_skeleton",
                    "black_skeleton",
                    "gold_skeleton",
                    "gold_skeleton",
                    "wisp",
                    "wisp",
                    "vampire",
                    "vampire",
                    "dark_knight",
                    "dark_knight"
            )

            else -> mutableListOf(
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton",
                    "skeleton"
            )
        }

        Collections.shuffle(mutable)
        return mutable.toTypedArray()
    }


    fun makeEnemy(room: RoomState, pos: Tile, group: Int, existingEnemies: Int): Enemy {
        val name = enemyMap[group]
        return when(name) {
            "skeleton" -> Skeleton(room, pos.copy(), existingEnemies)
            "gold_skeleton" -> GoldSkeleton(room, pos.copy(), existingEnemies)
            "black_skeleton" -> BlackSkeleton(room, pos.copy(), existingEnemies)
            "slime" -> Slime(room, pos.copy(), existingEnemies)
            "vampire" -> Vampire(room, pos.copy(), existingEnemies)
            "dark_knight" -> DarkKnight(room, pos.copy(), existingEnemies)
            "wisp" -> {
                if(RandomUtils.chance(0.5)) {
                    Wisp(room, pos.copy(), existingEnemies)
                } else {
                    BlueWisp(room, pos.copy(), existingEnemies)
                }
            }

            else -> {
                println("Invalid name: $name")
                Skeleton(room, pos.copy(), existingEnemies)
            }
        }
    }


    fun intValidOf(character: Char): Int = when(character) {
        '0' -> 0
        '1' -> 1
        '2' -> 2
        '3' -> 3
        '4' -> 4
        '5' -> 5
        '6' -> 6
        '7' -> 7
        '8' -> 8
        '9' -> 9
        else -> -1
    }

}