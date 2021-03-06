package com.game.state

import com.game.maths.RandomUtils
import com.game.maths.Tile

object RoomTemplates {

    fun startRoom(): Array<Pair<Char, Tile>> = arrayOf()

    fun bossRoom(difficulty: Int): Array<Pair<Char, Tile>> = when(difficulty) {

        0 -> arrayOf(
                Pair(   'w',    Tile(0, 0)     ),
                Pair(   'w',    Tile(0, 11)     ),
                Pair(   'w',    Tile(11, 0)     ),
                Pair(   'w',    Tile(11, 11)     ),
                Pair(   'b',    Tile(3, 3)     ),
                Pair(   'b',    Tile(3, 8)     ),
                Pair(   'b',    Tile(8, 3)     ),
                Pair(   'b',    Tile(8, 8)     )
        )

        1 -> arrayOf(
                Pair(   'b',    Tile(3, 3)     ),
                Pair(   'b',    Tile(3, 8)     ),
                Pair(   'b',    Tile(8, 3)     ),
                Pair(   'b',    Tile(8, 8)     ),
                Pair(   'b',    Tile(5, 3)     ),
                Pair(   'b',    Tile(3, 5)     )
        )

        2 -> arrayOf(
                Pair(   'b',    Tile(0, 0)     ),
                Pair(   'b',    Tile(0, 11)     ),
                Pair(   'b',    Tile(11, 0)     ),
                Pair(   'b',    Tile(11, 11)     ),
                Pair(   'b',    Tile(3, 3)     ),
                Pair(   'b',    Tile(3, 8)     ),
                Pair(   'b',    Tile(8, 3)     ),
                Pair(   'b',    Tile(8, 8)     )
        )

        else -> arrayOf()

    }

    fun exitRoom(): Array<Pair<Char, Tile>> = arrayOf()

    fun treasureRoom(difficulty: Int): Array<Pair<Char, Tile>> = arrayOf()

    fun combatRoom(difficulty: Int): Array<Pair<Char, Tile>> = when(RandomUtils.randRange(0..7)) {

        0 -> arrayOf(
                Pair(   'w',    Tile(3, 3)     ),
                Pair(   'w',    Tile(3, 9)     ),
                Pair(   'w',    Tile(9, 3)     ),
                Pair(   'w',    Tile(9, 9)     ),
                Pair(   '0',    Tile(4, 4)     ),
                Pair(   '0',    Tile(4, 8)     ),
                Pair(   '0',    Tile(8, 4)     ),
                Pair(   '0',    Tile(8, 8)     )
        )

        1 -> arrayOf(
                Pair(   '0',    Tile(4, 4)     ),
                Pair(   '0',    Tile(4, 8)     ),
                Pair(   '0',    Tile(8, 4)     ),
                Pair(   '0',    Tile(8, 8)     )
        )

        2 -> arrayOf(
                Pair(   'w',    Tile(3, 7)     ),
                Pair(   'w',    Tile(4, 7)     ),
                Pair(   'w',    Tile(5, 7)     ),
                Pair(   'w',    Tile(6, 7)     ),
                Pair(   'w',    Tile(7, 7)     ),
                Pair(   'w',    Tile(8, 7)     ),
                Pair(   '0',    Tile(4, 4)     ),
                Pair(   '0',    Tile(4, 6)     ),
                Pair(   '0',    Tile(4, 8)     )
        )

        3 -> arrayOf(
                Pair(   'w',    Tile(0, 0)     ),
                Pair(   'w',    Tile(0, 11)     ),
                Pair(   'w',    Tile(11, 0)     ),
                Pair(   'w',    Tile(11, 11)     ),
                Pair(   '0',    Tile(3, 3)     ),
                Pair(   '0',    Tile(3, 8)     ),
                Pair(   '0',    Tile(8, 8)     )
        )

        4 -> arrayOf(
                Pair(   '0',    Tile(1, 1)     ),
                Pair(   '0',    Tile(1, 10)     ),
                Pair(   '0',    Tile(10, 10)     ),
                Pair(   '0',    Tile(4, 4)     ),
                Pair(   '0',    Tile(7, 4)     ),
                Pair(   '0',    Tile(7, 7)     )
        )

        5 -> arrayOf(
                Pair(   'w',    Tile(5, 5)     ),
                Pair(   'w',    Tile(5, 6)     ),
                Pair(   'w',    Tile(6, 5)     ),
                Pair(   'w',    Tile(6, 6)     ),
                Pair(   '0',    Tile(4, 5)     ),
                Pair(   '0',    Tile(7, 5)     ),
                Pair(   '0',    Tile(5, 7)     ),
                Pair(   '1',    Tile(1, 1)     ),
                Pair(   '1',    Tile(1, 10)     ),
                Pair(   '1',    Tile(10,  1)     ),
                Pair(   '1',    Tile(10,  10)     )
        )

        6 -> arrayOf(
                Pair(   '0',    Tile(4, 5)     ),
                Pair(   '0',    Tile(7, 5)     ),
                Pair(   '0',    Tile(7, 7)     ),
                Pair(   '1',    Tile(1, 1)     ),
                Pair(   '1',    Tile(1, 10)     ),
                Pair(   '1',    Tile(10,  10)     )
        )

        7 -> arrayOf(
                Pair(   '0',    Tile(4, 5)     ),
                Pair(   '0',    Tile(7, 5)     ),
                Pair(   '0',    Tile(7, 7)     ),
                Pair(   '0',    Tile(1, 1)     ),
                Pair(   '0',    Tile(1, 10)     ),
                Pair(   '0',    Tile(10,  10)     )
        )

        else -> arrayOf()
    }
}