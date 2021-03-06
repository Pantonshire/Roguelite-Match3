package com.game.state

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.game.audio.SFX
import com.game.entity.*
import com.game.graphics.GameCanvas
import com.game.graphics.Sequences
import com.game.graphics.Textures
import com.game.maths.*
import com.game.particle.AnimatedParticle
import com.game.particle.Particle
import com.game.particle.TextParticle
import com.game.run.Run
import tilemap.TileMap

class RoomState(playerPos: Tile, val north: Boolean, val east: Boolean, val south: Boolean, val west: Boolean, val tiles: TileMap = TileMap(32, 20, 24, "tiles", 5)): State() {

    val particles: MutableList<Particle> = mutableListOf()
    val entities: MutableList<Entity> = mutableListOf()
    val player: Player = Player(this, playerPos)
    var alreadyCleared: Boolean = false
    val ladderPos: Tile = Tile(-1, -1)
    val ladderTexture: TextureRegion = TextureRegion(Textures.get("ladder"))

    var turnQueue: MutableList<Entity> = mutableListOf()
    val killSet: MutableSet<Entity> = mutableSetOf()
    var round = 0
    var delay = 0
    var lastEntity: Entity? = null
    var gameOver: Boolean = false
    var gameOverTicks: Int = 0
    var doorsLocked = false
    var enemyPathToShow: Int = -1


    init {
        entities.add(player)
    }


    override fun update() {
        particles.asSequence().forEach {
            it.update()
        }

        particles.removeIf { it.shouldRemove() }

        if(gameOver) {
            ++gameOverTicks
            if(gameOverTicks > 30 && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                StateManager.queue(MainMenu())
            }
            return
        }

        if(delay <= 0) {
            var removedEntities = false
            while(killSet.isNotEmpty()) {
                removedEntities = true
                killSet.asSequence().forEach {
                    killEntity(it)
                }
                killSet.clear()
                checkGroups()
            }

            if(removedEntities) {
                SFX.play("boom")
            }

            if(combat()) {
                if(turnQueue.isEmpty()) {
                    lastEntity?.endIdle()
                    newRound()
                }

                if(isPlayerTurn() && Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                    var nextId = -1
                    for(it in entities) {
                        if(it is Enemy) {
                            if(it.id > enemyPathToShow && (it.id < nextId || nextId == -1)) {
                                nextId = it.id
                            }
                        }
                    }

                    enemyPathToShow = nextId
                }

                val currentEntity = turnQueue.first()

                if(currentEntity.invincible) {
                    currentEntity.invincible = false
                }

                if(lastEntity != currentEntity) {
                    lastEntity?.endIdle()
                    currentEntity.startTurn()
                    lastEntity = currentEntity
                } else if(currentEntity.isFinished() || currentEntity.dead) {
                    turnQueue.removeAt(0)
                    currentEntity.endTurn()
                    delay = maxOf(delay, currentEntity.actionDelay())
                }

                entities.asSequence().forEach {
                    it.endIdle()
                }

                if(currentEntity.act()) {
                    delay = maxOf(delay, currentEntity.actionDelay())
                }
            } else {
                if(doorsLocked) {
                    openDoors()
                }

                if(ladderPos.x != -1 && ladderPos.y != -1 && ladderPos.x == player.pos.x && ladderPos.y == player.pos.y && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                    Run.current.nextFloor()
                    return
                }

                checkExitRoom()
                player.endIdle()
                if(player.act()) {
                    delay = maxOf(delay, player.actionDelay())
                }
            }
        } else {
            --delay
            if(combat()) {
                if(turnQueue.isNotEmpty()) {
                    turnQueue.first().idle()
                }
            } else {
                player.idle()
            }
        }
    }


    override fun drawGame(canvas: GameCanvas) {
        tiles.draw(canvas)

        if(!doorsLocked) {
            if(north) { canvas.draw(Textures.get("exit_arrow"), 384f, 396f, rotation = Angle(0.0)) }
            if(south) { canvas.draw(Textures.get("exit_arrow"), 384f, 84f, rotation = Angle(Math.PI)) }
            if(east) { canvas.draw(Textures.get("exit_arrow"), 540f, 240f, rotation = Angle(Math.PI / -2.0)) }
            if(west) { canvas.draw(Textures.get("exit_arrow"), 228f, 240f, rotation = Angle(Math.PI / 2.0)) }
            if(ladderPos.x != -1 && ladderPos.y != -1) {
                canvas.drawTile(ladderTexture, ladderPos.x * 24, ladderPos.y * 24)
            }
        }

        entities.asSequence().filter { it !is Enemy || enemyPathToShow == -1 || it.id == enemyPathToShow }.forEach { it.drawBG(canvas) }
        entities.asSequence().forEach { it.draw(canvas) }

        particles.asSequence().forEach {
            it.draw(canvas)
        }

        if(isPlayerTurn()) {
            entities.asSequence().filter { it !is Enemy || enemyPathToShow == -1 || it.id == enemyPathToShow }.forEach { it.drawFG(canvas) }

            for(i in 1 until turnQueue.size) {
                canvas.drawText(i.toString(),
                        turnQueue[i].pos.x.toFloat() * tiles.tileSize + 2,
                        turnQueue[i].pos.y.toFloat() * tiles.tileSize + tiles.tileSize + 6,
                        "prstart", 8, Color.WHITE)
            }
        }
    }


    override fun drawHUD(canvas: GameCanvas) {
        for(i in 0 until Run.current.maxHealth) {
            canvas.draw(Textures.get(if(i >= Run.current.health) "empty_heart" else "heart"),340f, 580f - 20f * i)
        }

        canvas.drawText("Floor ${Run.current.difficulty + 1}", 838f, 230f, "prstart", 8, Color.WHITE)

        if(combat()) {
            for(i in 0 until Run.current.movements) {
                canvas.draw(Textures.get(if(i >= player.movesLeft) "empty_boot" else "boot"), 360f, 580f - 20f * i)
            }

            for(i in 0 until Run.current.attacks) {
                canvas.draw(Textures.get(if(i >= player.attacksLeft) "empty_sword" else "sword"), 380f, 580f - 20f * i)
            }

            if(isPlayerTurn()) {
                canvas.drawText("Q: End turn", 840f, 580f, "prstart", 8, Color.WHITE)
                canvas.drawText("WASD: Move", 840f, 560f, "prstart", 8, Color.WHITE)
                canvas.drawText("Arrow keys:", 838f, 540f, "prstart", 8, Color.WHITE)
                canvas.drawText("Attack", 838f, 528f, "prstart", 8, Color.WHITE)
                canvas.drawText("Tab: view", 838f, 508f, "prstart", 8, Color.WHITE)
                canvas.drawText("a single", 838f, 496f, "prstart", 8, Color.WHITE)
                canvas.drawText("enemy\'s path", 838f, 484f, "prstart", 8, Color.WHITE)
            } else {
                canvas.drawText("Enemy\'s turn", 838f, 580f, "prstart", 8, Color.WHITE)
            }
        } else {
            if(ladderPos.x != -1 && ladderPos.y != -1 && ladderPos.x == player.pos.x && ladderPos.y == player.pos.y) {
                canvas.drawText("Space: Descend", 532f, 240f, "prstart", 16, Color.WHITE)
            }

            if(!alreadyCleared) {
                canvas.drawText("Victory!", 838f, 580f, "prstart", 8, Color.WHITE)
                canvas.drawText("Time to go to", 838f, 568f, "prstart", 8, Color.WHITE)
                canvas.drawText("the next room", 838f, 556f, "prstart", 8, Color.WHITE)
            } else {
                canvas.drawText("Phew, no", 838f, 580f, "prstart", 8, Color.WHITE)
                canvas.drawText("enemies here!", 838f, 568f, "prstart", 8, Color.WHITE)
                canvas.drawText("WASD: Move", 838f, 548f, "prstart", 8, Color.WHITE)
            }
        }

        if(gameOverTicks > 30) {
            canvas.drawText("GAME OVER (PRESS SPACE)", 460f, 420f, "prstart", 16, Color.WHITE)
        }
    }


    fun newRound() {
        ++round
        delay = 40
        enemyPathToShow = -1
        turnQueue = entities.sortedWith(compareBy({ -it.currentSpeed() })).toMutableList()
        lastEntity = null
        chooseEnemyIntentions()
    }

    private fun killEntity(entity: Entity) {
        if(!entity.dead && !entity.invincible) {
            entities.remove(entity)
            if (entity in turnQueue) {
                turnQueue.remove(entity)
            }

            particles.add(0, AnimatedParticle(entity.drawPos(), Vector(), "explosion", Sequences.explosion))
            entity.endIdle()
            entity.dead = true
            entity.onDied()
        }
    }


    private fun checkGroups() {
        entities.asSequence().forEach {
            if(it is Enemy) {
                val enemy: Enemy = it
                val group = it.group
                var others = 0
                entities.asSequence().forEach {
                    if(it != enemy && it is Enemy) {
                        if(it.group == group) {
                            ++others
                        }
                    }
                }

                if(others < 2) {
                    entities.filter { it is Enemy && it.group == group && !it.invincible }.forEach { killSet += it }
                }
            }
        }
    }


    private fun chooseEnemyIntentions() {
        turnQueue.asSequence().forEach {
            if(it is Enemy) {
                it.chooseIntentions()
            }
        }
    }


    private fun isPlayerTurn(): Boolean = !turnQueue.isEmpty() && turnQueue.first() is Player


    private fun checkExitRoom() {
        val exitDirection: Direction? = when {
            player.pos.x < 10 -> Direction.WEST
            player.pos.x > 21 -> Direction.EAST
            player.pos.y < 4 -> Direction.SOUTH
            player.pos.y > 15 -> Direction.NORTH
            else -> null
        }

        if(exitDirection != null) {
            Run.current.travel(exitDirection)
        }
    }


    fun isEmpty(tile: Tile, futurePositions: Boolean = false, vararg ignore: Entity): Boolean {
        if(tiles.isSolid(tile)) {
            return false
        }

        entities.asSequence().forEach {
            var entityX = it.pos.x
            var entityY = it.pos.y
            if(futurePositions && it is Enemy) {
                entityX = it.futurePos.x
                entityY = it.futurePos.y
            }

            if(it !in ignore && entityX == tile.x && entityY == tile.y) {
                return false
            }
        }

        return true
    }


    fun entityAt(tile: Tile, futurePositions: Boolean = false, vararg ignore: Entity): Entity? {
        entities.asSequence().forEach {
            var entityX = it.pos.x
            var entityY = it.pos.y
            if(futurePositions && it is Enemy) {
                entityX = it.futurePos.x
                entityY = it.futurePos.y
            }

            if(it !in ignore && entityX == tile.x && entityY == tile.y) {
                return it
            }
        }

        return null
    }


    fun checkForMatch() {
        var foundMatch = false
        var textPos = Vector(0.0, 0.0)

        entities.asSequence().forEach {
            if(it is Enemy) {
                val rootEntity = it
                val rootPos = it.pos

                Direction.values().asSequence().forEach {
                    val chain: MutableSet<Enemy> = mutableSetOf(rootEntity)
                    for(distance in 1..10) {
                        val nextEntity = entityAt(rootPos.offset(it, distance))
                        if(nextEntity is Enemy) {
                            if(nextEntity.group == rootEntity.group && !nextEntity.invincible) {
                                chain += nextEntity
                            } else {
                                break
                            }
                        } else {
                            break
                        }
                    }

                    if(chain.size >= 3) {
                        killSet.addAll(chain)
                        if(!foundMatch) {
                            foundMatch = true
                            textPos = chain.first().drawPos()
                        }
                    }
                }
            }
        }

        if(foundMatch) {
            particles.add(TextParticle(textPos, Vector(y = 0.25), 60, RandomUtils.randEncouragement(), "prstart", 8, Color.WHITE).setTimer(10))
        }
    }


    fun damagePlayer() {
        SFX.play("boom")
        Run.current.loseHeart()
        particles.add(AnimatedParticle(player.drawPos(), Vector(), "hurt", Sequences.smallExplosion))
        if(Run.current.health <= 0) {
            gameOver = true
            entities.remove(player)
        }
    }


    fun combat(): Boolean = entities.size > 1


    fun openDoors() {
        doorsLocked = false
        if(north) {
            tiles.tiles[15][16] = 1
            tiles.tiles[16][16] = 1
        }
        if(south) {
            tiles.tiles[15][3] = 1
            tiles.tiles[16][3] = 1
        }
        if(east) {
            tiles.tiles[22][9] = 1
            tiles.tiles[22][10] = 1
        }
        if(west) {
            tiles.tiles[9][9] = 1
            tiles.tiles[9][10] = 1
        }
    }


    fun closeDoors() {
        doorsLocked = true
        if(north) {
            tiles.tiles[15][16] = 6
            tiles.tiles[16][16] = 6
        }
        if(south) {
            tiles.tiles[15][3] = 6
            tiles.tiles[16][3] = 6
        }
        if(east) {
            tiles.tiles[22][9] = 7
            tiles.tiles[22][10] = 7
        }
        if(west) {
            tiles.tiles[9][9] = 7
            tiles.tiles[9][10] = 7
        }
    }

}