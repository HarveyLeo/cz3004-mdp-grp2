package algorithms;

import java.util.ArrayList;
import java.util.Collections;

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
	private int[] _robotPosition;
	private Orientation _robotOrientation;
	
	private AStarPathFinder(int[][] mazeRef) {
		_virtualMap = new VirtualMap(mazeRef);
		_closed = new ArrayList<Node>();
		_open = new SortedList<Node>();
		_nodes = new Node[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
		for (int x = 0; x < Arena.MAP_LENGTH; x++) {
			for (int y = 0; y < Arena.MAP_WIDTH; y++) {
				_nodes[x][y] = new Node(x, y);
			}
		}
		_robot = new Robot();
		_robotPosition = new int[2];
		_robotPosition[0] = MazeExplorer.START[0];
		_robotPosition[1] = MazeExplorer.START[1];
		_robotOrientation = Orientation.NORTH;
	}
	
    public static AStarPathFinder getInstance() {
        if (_instance == null) {
        	MazeExplorer mazeExplorer = MazeExplorer.getInstance();
            _instance = new AStarPathFinder(mazeExplorer.getMazeRef());
        }
        return _instance;
    }
	
	public Path findPath() {
		
		_closed.clear();
		_open.clear();

		_nodes[MazeExplorer.START[0]][MazeExplorer.START[1]]._pathCost = 0;
		_open.add(_nodes[MazeExplorer.START[0]][MazeExplorer.START[1]]);
		
		//testing
		boolean[][] cleared = _virtualMap.getCleared();
		int value;
		for (int a = Arena.MAP_WIDTH - 1; a >= 0; a--) {
			for (int b =0; b < Arena.MAP_LENGTH; b++) {
				if (cleared[b][a]) {
					value = 1;
				} else {
					value = 0;
				}
				System.out.print(value + " ");
			}
			System.out.println();
		}

		
		while (_open.size() != 0) {
			Node current = _open.getFirstNode();
			if (current == _nodes[MazeExplorer.GOAL[0]][MazeExplorer.GOAL[1]]) {
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
						int pathCostOfNeighbor = current._pathCost + getEdgeCost(_robotOrientation, current._x, current._y, neighborX, neighborY);
						int heuristicOfNeighbor = getHeuristicCost(neighborX, neighborY);
						Node neighbor = _nodes[neighborX][neighborY];
						if (_virtualMap.checkIfVisited(neighborX, neighborY) == false) {
							neighbor._pathCost = pathCostOfNeighbor;
							neighbor._heuristic = heuristicOfNeighbor;
							neighbor._parent = current;
							addToOpen(neighbor);
							_virtualMap.setVisited(neighborX, neighborY);
						} else if (isInOpenList(neighbor)){
							if (pathCostOfNeighbor + heuristicOfNeighbor < neighbor._pathCost + neighbor._heuristic) {
								removeFromOpen(neighbor);
								neighbor._pathCost = pathCostOfNeighbor;
								neighbor._heuristic = heuristicOfNeighbor;
								neighbor._parent = current;
								addToOpen(neighbor);	
							}
						}
					}
				}
			}
		}
		
		Path path = new Path();
		Node target = _nodes[MazeExplorer.GOAL[0]][MazeExplorer.GOAL[1]];
		while (target != _nodes[MazeExplorer.START[0]][MazeExplorer.START[1]]) {
			path.prependStep(target._x, target._y);
			target = target._parent;
		}
		path.prependStep(MazeExplorer.START[0],MazeExplorer.START[1]);
		
		System.out.println(path.toString());
		
		return path;
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

	private int getHeuristicCost(int x, int y) {
		int heuristic;
		heuristic = ( MazeExplorer.GOAL[0] - x ) + ( MazeExplorer.GOAL[1] - y );
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
