package tilemap

import com.game.entity.Enemy
import com.game.maths.Direction
import com.game.maths.Tile
import com.game.state.RoomState

class PathFinder(val room: RoomState) {

    var nodes: MutableMap<Tile, Node> = mutableMapOf()
    var start: Tile = Tile(0, 0)
    var destination: Tile = Tile(0, 0)
    var avoidGroup: String = "none"


    fun getDirectionSequence(start: Tile, destination: Tile, avoidGroup: String): MutableList<Direction> {
        val pointPath = findPath(start, destination, avoidGroup)
        val directionPath: MutableList<Direction> = mutableListOf()

        var currentPos: Tile = start

        pointPath.asSequence().forEach {
            directionPath += when {
                currentPos.x < it.x -> Direction.EAST
                currentPos.x > it.x -> Direction.WEST
                currentPos.y < it.y -> Direction.NORTH
                currentPos.y > it.y -> Direction.SOUTH
                else -> Direction.NORTH
            }

            currentPos = it
        }

        return directionPath
    }


    fun findPath(start: Tile, destination: Tile, avoidGroup: String): MutableList<Tile> {
        this.start = start
        this.destination = destination
        this.avoidGroup = avoidGroup
        nodes = mutableMapOf()

        val path: MutableList<Tile> = mutableListOf()

        val closed: MutableSet<Node> = mutableSetOf()
        val open: MutableSet<Node> = mutableSetOf()
        val startNode: Node = getNode(start)
        var current: Node = startNode
        open += startNode

        var success = false
        var loops = 0

        while(open.isNotEmpty() && loops < 1000) {
            current = getCheapestOpenNode(open) ?: break

            if(current.pos == destination) {
                success = true
                break
            }

            open.remove(current)
            closed += current

            val neighbours = getNeighbours(current)
            var neighbourIsDestination = false

            neighbours.asSequence().filter { it !in closed }.forEach {
                if(it.pos == destination) {
                    neighbourIsDestination = true
                }

                if(room.isEmpty(it.pos)) {
                    if(it !in open) {
                        open += it
                    }

                    val newG = current.g + getWeight(it)
                    if(it.g < 0f || newG < it.g) {
                        it.g = newG
                        it.previous = current
                    }
                }
            }

            if(neighbourIsDestination) {
                success = true
                break
            }

            ++loops
        }

        if(success) {
            var reversePathNode: Node? = current
            while(reversePathNode != null && reversePathNode != startNode) {
                path.add(0, reversePathNode.pos)
                reversePathNode = reversePathNode.previous
            }
        }

        return path
    }


    private fun getCheapestOpenNode(open: Set<Node>): Node? {
        var cheapest: Node? = null
        open.asSequence().forEach {
            if(cheapest == null) {
                cheapest = it
            } else if(it.fScore() < cheapest!!.fScore()) {
                cheapest = it
            }
        }

        return cheapest
    }


    private fun getNeighbours(node: Node): Set<Node> {
        val neighbours: MutableSet<Node> = mutableSetOf()
        if(node.pos.x > 0) { neighbours += getNode(node.pos.offset(Direction.WEST)) }
        if(node.pos.y > 0) { neighbours += getNode(node.pos.offset(Direction.SOUTH)) }
        if(node.pos.x < room.tiles.width - 1) { neighbours += getNode(node.pos.offset(Direction.EAST)) }
        if(node.pos.y < room.tiles.height - 1) { neighbours += getNode(node.pos.offset(Direction.NORTH)) }
        return neighbours
    }


    private fun getNode(pos: Tile): Node {
		if(!nodes.containsKey(pos)) {
			val node = Node(pos, heuristic(pos))
			nodes[pos] = node
            return node
		}

		return nodes[pos]!!
	}


    private fun heuristic(pos: Tile): Float {
        val dx = (destination.x - pos.x).toFloat()
        val dy = (destination.y - pos.y).toFloat()
        return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }


    private fun getWeight(node: Node): Int {
        var consecutiveEnemies = 0
        var chainedEnemies = 0

        val neighbours = getNeighbours(node)
        neighbours.asSequence().forEach {
            val neighbourEntity = room.entityAt(it.pos)
            if(neighbourEntity is Enemy) {
                if(neighbourEntity.group == avoidGroup) {
                    ++consecutiveEnemies

                    val direction = when {
                        it.pos.x > node.pos.x -> Direction.EAST
                        it.pos.x < node.pos.x -> Direction.WEST
                        it.pos.y > node.pos.y -> Direction.NORTH
                        it.pos.y < node.pos.y -> Direction.SOUTH
                        else -> Direction.NORTH
                    }

                    val chainEntity = room.entityAt(it.pos.offset(direction))
                    if(chainEntity is Enemy) {
                        if(chainEntity.group == avoidGroup) {
                            ++chainedEnemies
                        }
                    }
                }
            }
        }

        if(consecutiveEnemies > 1) {
            chainedEnemies += consecutiveEnemies - 1
            consecutiveEnemies = 1
        }

        return 1 + 5 * consecutiveEnemies + 100 * chainedEnemies
    }

}