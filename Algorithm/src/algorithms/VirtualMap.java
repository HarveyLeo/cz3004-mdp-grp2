package algorithms;

import simulator.arena.Arena;

public class VirtualMap {
	
	private boolean[][] _visited;
	private boolean[][] _cleared;
	
	public VirtualMap(int[][] mazeRef) {
		
		//testing - check result of exploration
/*		System.out.println("=========Explore Result===========");
		for (int a = Arena.MAP_WIDTH - 1; a >= 0; a--) {
			for (int b =0; b < Arena.MAP_LENGTH; b++) {
				System.out.print(mazeRef[b][a] + " ");
			}
			System.out.println();
		}
		System.out.println("==============END=================");*/
		
		_visited = new boolean[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
		_cleared = new boolean[Arena.MAP_LENGTH][Arena.MAP_WIDTH];
		
		for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				_visited[i][j] = false;
				_cleared[i][j] = true;
			}
		}
		
		for (int i = 0; i < Arena.MAP_LENGTH; i++) {
			for (int j = 0; j < Arena.MAP_WIDTH; j++) {
				if (i == 0 || i == Arena.MAP_LENGTH - 1 || j == 0 || j == Arena.MAP_WIDTH - 1) {
					_cleared[i][j] = false;
				} else if (mazeRef[i][j] != MazeExplorer.IS_EMPTY) {
					for (int u = i - 1; u <= i + 1; u++) {
						for (int v = j - 1; v <= j + 1; v++) {
							_cleared[u][v] = false;
						}
					}
				} 
			}
		}
	}

	public boolean[][] getCleared() {
		return _cleared;
	}

	public boolean checkIfVisited(int x, int y) {
		return _visited[x][y];
	}

	public void setVisited(int x, int y) {
		_visited[x][y] = true;
		
	}
	
	
	
	
	
}
