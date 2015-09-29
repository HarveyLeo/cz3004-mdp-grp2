package algorithms;

import java.util.ArrayList;
import java.util.Collections;

import datatypes.Movement;
import datatypes.Orientation;
import simulator.arena.Arena;
import simulator.robot.Robot;

public class AStarPathFinder {
	
	private static AStarPathFinder _instance;
	private VirtualMap _virtualMap;
	private ArrayList<Node> _closed;
	private SortedList<Node> _open;
	private Node[][] _nodes;
	private Robot _robot;
	
    public static AStarPathFinder getInstance() {
        if (_instance == null) {
            _instance = new AStarPathFinder();
        }
        return _instance;
    }
	
	public Path findFastestPath(int startX, int startY, int destinationX, int destinationY, int[][] mazeRef) {
		
		init(mazeRef);
		
		_closed.clear();
		_open.clear();

		_nodes[startX][startY]._pathCost = 0;
		_nodes[startX][startY]._ori = Orientation.NORTH;
		_open.add(_nodes[startX][startY]);
		
		//testing - check virtual map
//		boolean[][] cleared = _virtualMap.getCleared();
//		int value;
//		for (int a = Arena.MAP_WIDTH - 1; a >= 0; a--) {
//			for (int b = 0; b < Arena.MAP_LENGTH; b++) {
//				if (cleared[b][a]) {
//					value = 1;
//				} else {
//					value = 0;
//				}
//				System.out.print(value + " ");
//			}
//			System.out.println();
//		}

		
		while (_open.size() != 0) {
			Node current = _open.getFirstNode();
			if (current == _nodes[destinationX][destinationY]) {
				break;
			}
			_open.remove(current);
			_closed.add(current);

			for (int i = -1; i < 2; i++) {
				for (int j = -1; j < 2; j++) {
					if (i == 0 && j == 0) {
						continue;
					}
					if ((i != 0) && (j != 0)) {
						continue;
					}

					int neighborX = current._x + i;
					int neighborY = current._y + j; 
					

					if (isValidLocation(_virtualMap.getCleared(), neighborX, neighborY)) {
						int pathCostOfNeighbor = current._pathCost + getEdgeCost(current._ori, current._x, current._y, neighborX, neighborY);
						int heuristicOfNeighbor = getHeuristicCost(neighborX, neighborY, destinationX, destinationY);
						Node neighbor = _nodes[neighborX][neighborY];
						if (_virtualMap.checkIfVisited(neighborX, neighborY) == false) {
							neighbor._pathCost = pathCostOfNeighbor;
							neighbor._heuristic = heuristicOfNeighbor;
							neighbor._parent = current;
							neighbor._ori = getOrientationIfMoveToNeighbor(current._ori, current._x, current._y, neighborX, neighborY);
							addToOpen(neighbor);
							_virtualMap.setVisited(neighborX, neighborY);
						} else if (isInOpenList(neighbor)){
							if (pathCostOfNeighbor + heuristicOfNeighbor < neighbor._pathCost + neighbor._heuristic) {
								removeFromOpen(neighbor);
								neighbor._pathCost = pathCostOfNeighbor;
								neighbor._heuristic = heuristicOfNeighbor;
								neighbor._parent = current;
								neighbor._ori = getOrientationIfMoveToNeighbor(current._ori, current._x, current._y, neighborX, neighborY);
								addToOpen(neighbor);	
							}
						}
					}
				}
			}
		}
		
		Path path = new Path();
		Node target = _nodes[destinationX][destinationY];
		while (target != _nodes[startX][startY]) {
			path.prependStep(target._x, target._y);
			target = target._parent;
		}
		path.prependStep(startX,startY);
				
		return path;
	}

	private void init(int[][] mazeRef) {
		_virtualMap = VirtualMap.getInstance();
		_virtualMap.updateVirtualMap(mazeRef);
		_closed = new ArrayList<Node>();
		_open = new SortedList<Node>();
		_nodes = new Node[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
		for (int x = 0; x < Arena.MAP_LENGTH; x++) {
			for (int y = 0; y < Arena.MAP_WIDTH; y++) {
				_nodes[x][y] = new Node(x, y);
			}
		}
		_robot = Robot.getInstance();
	}

	public Orientation moveRobotAlongFastestPath(Path fastestPath, Orientation currentOrientation, boolean isExploring) {

		int[] tempPosition = new int[2];
		int[] robotPosition;
		int[] nextPosition = new int[2];
		Orientation nextOrientation;
		ArrayList<Path.Step> steps = fastestPath.getSteps();
		MazeExplorer explorer = MazeExplorer.getInstance();
		
		robotPosition = fastestPath.getStep(0);
		
		int count = 0;
		
		for (int i = 0; i < steps.size() - 1; i++) {
			
			tempPosition[0] = steps.get(i).getX();
			tempPosition[1] = steps.get(i).getY();
			nextPosition[0] = steps.get(i+1).getX();
			nextPosition[1] = steps.get(i+1).getY();

			nextOrientation = getOrientationIfMoveToNeighbor(currentOrientation, 
					tempPosition[0], tempPosition[1],
					nextPosition[0], nextPosition[1]);
			if (nextOrientation == currentOrientation) {
				count++;
			} else {

				if (isExploring) {
					for (int v = 0; v < count; v++) {
						robotPosition = explorer.updateRobotPositionAfterMF(currentOrientation, robotPosition);
						explorer.setIsExplored(robotPosition, currentOrientation);
						_robot.moveForward();
					}
				} else {
					_robot.moveForward(count);
				}
				count = 1;
				Movement move = ChangeRobotOrientation(currentOrientation, nextOrientation);
				Orientation ori;
				if (isExploring) {
					if (move == Movement.TURN_RIGHT_TWICE) {
						ori = explorer.updateRobotOrientation(Movement.TURN_RIGHT);
						explorer.setIsExplored(robotPosition, ori);
						explorer.updateRobotOrientation(Movement.TURN_RIGHT);
						explorer.setIsExplored(robotPosition, ori);
					} else {
						ori = explorer.updateRobotOrientation(move);
						explorer.setIsExplored(robotPosition, ori);
					}
				}
			}
			currentOrientation = nextOrientation;
		}


		if (isExploring) {
			for (int v = 0; v < count; v++) {
				robotPosition = explorer.updateRobotPositionAfterMF(currentOrientation, robotPosition);
				explorer.setIsExplored(robotPosition, currentOrientation);
				_robot.moveForward();
			}
		} else {
			_robot.moveForward(count);
		}
		
		return currentOrientation;
		
	}



	private Movement ChangeRobotOrientation(Orientation curOri, Orientation nextOri) {
		switch (curOri) {
			case NORTH:
				if (nextOri == Orientation.EAST) {
					_robot.turnRight();
					return Movement.TURN_RIGHT;
				} else if (nextOri == Orientation.WEST) {
					_robot.turnLeft();
					return Movement.TURN_LEFT;
				} else if (nextOri == Orientation.SOUTH) {
					_robot.turnRight();
					_robot.turnRight();
					return Movement.TURN_RIGHT_TWICE;
				}
				break;
			case SOUTH:
				if (nextOri == Orientation.EAST) {
					_robot.turnLeft();
					return Movement.TURN_LEFT;
				} else if (nextOri == Orientation.WEST) {
					_robot.turnRight();
					return Movement.TURN_RIGHT;
				} else if (nextOri == Orientation.NORTH) {
					_robot.turnRight();
					_robot.turnRight();
					return Movement.TURN_RIGHT_TWICE;
				}
				break;
			case EAST:
				if (nextOri == Orientation.NORTH) {
					_robot.turnLeft();
					return Movement.TURN_LEFT;
				} else if (nextOri == Orientation.SOUTH) {
					_robot.turnRight();
					return Movement.TURN_RIGHT;
				} else if (nextOri == Orientation.WEST) {
					_robot.turnRight();
					_robot.turnRight();
					return Movement.TURN_RIGHT_TWICE;
				}
				break;
			case WEST:
				if (nextOri == Orientation.NORTH) {
					_robot.turnRight();
					return Movement.TURN_RIGHT;
				} else if (nextOri == Orientation.SOUTH) {
					_robot.turnLeft();
					return Movement.TURN_LEFT;
				} else if (nextOri == Orientation.EAST) {
					_robot.turnRight();
					_robot.turnRight();
					return Movement.TURN_RIGHT_TWICE;
				}
		}
		return null;
	}

	private Orientation getOrientationIfMoveToNeighbor(Orientation curOri, int curX, int curY, int nextX, int nextY) {
		Orientation nextOri = null;
	
		if (nextY == curY + 1) {
			nextOri = Orientation.NORTH;
		} else if (nextY == curY - 1) {
			nextOri = Orientation.SOUTH;
		} else if (nextX == curX + 1) {
			nextOri = Orientation.EAST;
		} else if (nextX == curX - 1) {
			nextOri = Orientation.WEST;
		}
				
		return nextOri;
	}

	private void removeFromOpen(Node node) {
		_open.remove(node);
	}

	private boolean isInOpenList(Node node) {
		return _open.contains(node);
	}

	private void addToOpen(Node node) {
		_open.add(node);	
	}

	private int getHeuristicCost(int x, int y, int destinationX, int destinationY) {
		int heuristic;
		heuristic = ( destinationX - x ) + ( destinationY - y );
		return heuristic;
	}

	private int getEdgeCost(Orientation robotOrientation, int currentX, int currentY, int nextX, int nextY) {
		int edgeCost = 0;
		switch (robotOrientation) {
			case NORTH:
				if (nextY == currentY + 1) {
					edgeCost = 1;
				} else if (nextX != currentX) {
					edgeCost = 2;
				} else if (nextY == currentY - 1) {
					edgeCost = 3;
				}
				break;
			case SOUTH:
				if (nextY == currentY - 1) {
					edgeCost = 1;
				} else if (nextX != currentX) {
					edgeCost = 2;
				} else if (nextY == currentY + 1) {
					edgeCost = 3;
				}
				break;
			case EAST:
				if (nextX == currentX + 1) {
					edgeCost = 1;
				} else if (nextY != currentY) {
					edgeCost = 2;
				} else if (nextX == currentX - 1) {
					edgeCost = 3;
				}
				break;
			case WEST:
				if (nextX == currentX - 1) {
					edgeCost = 1;
				} else if (nextY != currentY) {
					edgeCost = 2;
				} else if (nextX == currentX + 1) {
					edgeCost = 3;
				}
		}
		return edgeCost;
	}

	private boolean isValidLocation(boolean[][] cleared, int x, int y) {
		boolean valid;
		if ( x < 0 || y < 0 || x >= Arena.MAP_LENGTH || y >= Arena.MAP_WIDTH) {
			valid = false;
		} else {
			valid = cleared[x][y];
		}
		
		return valid;
	}
	
	private class SortedList<T extends Comparable<T>> {
		
		private ArrayList<T> _list = new ArrayList<T>();
		
		public T getFirstNode() {
			return _list.get(0);

		}
		
		public void clear() {
			_list.clear();
		}
			
		public void add(T element) {
			_list.add(element);
			Collections.sort(_list);
		}
		
		public void remove(T element) {
			_list.remove(element);
		}
	
		public int size() {
			return _list.size();
		}
		
		public boolean contains(T element) {
			return _list.contains(element);
		}
	}
	
	private class Node implements Comparable<Node> {

		private int _x;
		private int _y;
		private int _pathCost;
		private Node _parent;
		private Orientation _ori;
		private int _heuristic;

		public Node(int x, int y) {
			this._x = x;
			this._y = y;
		}
		
		@Override
		public int compareTo(Node other) {
			
			int f = _heuristic + _pathCost;
			int otherf = other._heuristic + other._pathCost;
			
			if (f < otherf) {
				return -1;
			} else if (f > otherf) {
				return 1;
			} else {
				return 0;
			}
		}
	}

}
