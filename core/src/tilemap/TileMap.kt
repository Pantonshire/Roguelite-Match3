package tilemap

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.game.graphics.GameCanvas
import com.game.graphics.Textures
import com.game.maths.RandomUtils
import com.game.maths.Tile
import com.game.maths.Vector

class TileMap(val width: Int, val height: Int, val tileSize: Int, tileSet: String, val tilesPerRow: Int) {

    val nullTile: Byte = 0.toByte()

    val tiles: Array<Array<Byte>> = Array(width, { Array(height, {
        if(RandomUtils.chance(0.75)) 1.toByte() else RandomUtils.randRange(2..4).toByte()
    }) })

    val tileSet: TextureRegion = TextureRegion(Textures.get(tileSet))
    var lastDrawnTile: Byte = 0

    fun draw(canvas: GameCanvas) {
        val startX  = maxOf(0, ((canvas.camera.position.x - canvas.scrWidth / 2) / tileSize).toInt() - 1)
        val endX    = minOf(width - 1, ((canvas.camera.position.x + canvas.scrWidth / 2) / tileSize).toInt() + 1)
        val startY  = maxOf(0, ((canvas.camera.position.y - canvas.scrHeight / 2) / tileSize).toInt() - 1)
        val endY    = minOf(height - 1, ((canvas.camera.position.y + canvas.scrHeight / 2) / tileSize).toInt() + 1)

        for(x in startX..endX) {
            for(y in startY..endY) {
                val tile: Byte = tiles[x][y]
                if(tile == nullTile) {
                    continue
                } else if(lastDrawnTile != tile) {
                    tileSet.setRegion(colOf(tile) * tileSize, rowOf(tile) * tileSize, tileSize, tileSize)
                    lastDrawnTile = tile
                }

                canvas.drawTile(tileSet, x * tileSize, y * tileSize)
            }
        }
    }

    fun isSolid(tile: Tile) = tile.x !in (0 until width) || tile.y !in (0 until height) || rowOf(tiles[tile.x][tile.y]) == 1

    fun getMapCoordinates(pos: Vector): Tile = Tile((pos.x / tileSize).toInt(), (pos.y / tileSize).toInt())

    fun getPositionOf(tile: Tile): Vector = Vector((tile.x * tileSize + tileSize / 2).toDouble(), (tile.y * tileSize + tileSize / 2).toDouble())

    fun rowOf(tile: Byte) = tile / tilesPerRow

    fun colOf(tile: Byte) = tile % tilesPerRow

}