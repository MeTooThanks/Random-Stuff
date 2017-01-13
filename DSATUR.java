import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class DSATUR {
	private int ub, lb, cn, vertices, edges;
	private ColEdge[] graph;
	int[] assignedColors;
	private int[][] adjacencyMatrix, adjacencySimple, DSAT;
	int[][] forbiddenColors;
	int[][] saturation;
	boolean[] removed;
	ArrayList<Integer> removedVertices;

	public DSATUR(ColEdge[] e, int initVertices, int initEdges) {
		graph = e;
		vertices = initVertices;
		edges = initEdges;
		ub = 0;
		lb = 1;
		adjacencyMatrix = new int[vertices][vertices];
		removed = new boolean[vertices];
		adjacency();
	    DSATUR();			
	}

	public void adjacency() {
		for (ColEdge edge : graph) {
			adjacencyMatrix[edge.u-1][edge.v-1] = 1;
			adjacencyMatrix[edge.v-1][edge.u-1] = 1;
		}
		
		adjSimple();
	}
	
	public void adjSimple() {
		adjacencySimple = new int[vertices][vertices];
		for (int i = 1; i < vertices; i++) {
			for (int j = 0; j < i; j++) {
				adjacencySimple[i][j] = adjacencyMatrix[i][j];
			}
		}
	}
	
	public void DSATUR() {
		forbiddenColors = new int[vertices][vertices];
		assignedColors = new int[vertices];
		int tempK = 0;
		boolean found;
		removedVertices = new ArrayList<Integer>();
		
		//is set for every row, which adjacent vertices have which color
		boolean[] usedColors = new boolean[vertices];
		usedColors[0] = true;
		int nodeBefore = 0;
		int vertex = 0;
		
		createDSATMatrix();
		
		for (int m = 0; m < vertices; m++) {
			vertex = choseVertex();
			
			System.out.println(vertices-m);
			
			for (int j = 0; j < vertices; j++)
				usedColors[forbiddenColors[vertex][j]] = true; //sets the colors of the adjacent nodes as used
				
			found = true;
			
			//assigns the first unused color
			for (int k = 1; k < vertices && found; k++) {
				assignedColors[vertex] = k;
				tempK = k;
				found = usedColors[k];
			}
			//sets the used color for the just colored node as a forbidden color for every adjacent node
			for (int row = 1; row < vertices; row++)
				forbiddenColors[row][vertex] = (adjacencySimple[row][vertex] == 1) ? tempK : 0;
			
			//resets the matrix of used colors
			usedColors = new boolean[vertices];
			usedColors[0] = true;
			
			removed[vertex] = true;
			nodeBefore = vertex;
			int index = 0;
			//System.out.println(DSAT[vertex][3]);
			updateDSATMatrix(vertex);
		}
		
		//final evaluation which colors have been used
		for (int i = 0; i < assignedColors.length; i ++)
			usedColors[assignedColors[i]] = true;
		
		//looks for the first unused color, the index-1 of that color is the upper bound
		for (int i = 1; i < usedColors.length; i++)
			if (usedColors[i] == true){
				ub++;
			}
		
		System.out.println("NEW BEST UPPER BOUND: " +ub);
		System.out.println("NEW BEST LOWER BOUND: " +lb);
	}
	
	public int choseVertex() {	
		sort(1, DSAT);
		int index = 0;
		while (removed[DSAT[index][0]] == true) {
			index++;
		}
		if (index+1 < DSAT.length && DSAT[index][1] == DSAT[index+1][1]) {
			int tempIndex = index;
			
			while (index+1 < DSAT.length && DSAT[index][1] == DSAT[index+1][1]) {
				index++;
			}
			
			int[][] subArray = new int[index-tempIndex+1][3];
			
			for (int i = 0; i < subArray.length; i++)
				for (int j = 0; j < DSAT[0].length; j++) {
					subArray[i][j] = DSAT[tempIndex][j];
				}
			
			sort(2, subArray);
			return subArray[0][0];
			
		} else {
			return DSAT[index][0];
		}
	}
		
	
	//GETESTET, FUNKTIONIERT
	public void sort(int column, int[][] array) {
		Arrays.sort(array, new Comparator<int[]>() {
            @Override
            public int compare(final int[] first, final int[] second) {
            	  return Integer.compare(second[column], first[column]);
            }
        });
	}
	
	//SHOULD BE OK
	public void createDSATMatrix() {
		DSAT = new int[vertices][3];
		for (int i = 0; i < vertices; i++) {
			DSAT[i][0] = i;
			for (int j = 0; j < vertices; j++) {
				if (adjacencySimple[i][j] == 1)
					DSAT[i][2]++;
			}
		}
	}
	
	public void updateDSATMatrix(int nodeBefore) {
		for (int i = 0; i < vertices; i++) {
			if (removed[i] == false && adjacencySimple[nodeBefore][i] == 1) {
				if (checkIfColorIsAlreadyAdjacent(i, nodeBefore)) {
					int index = 0;
					while (i != DSAT[index][0]) {
						index++;
					}
					DSAT[index][1]++;
				}
			}
		}
	}
	
	public boolean checkIfColorIsAlreadyAdjacent(int index, int nodeBefore) {
		for (int i = 0; i < vertices; i++) {
			if (forbiddenColors[index][i] == assignedColors[nodeBefore] && i != nodeBefore)
				return false;
		}
		return true;
	}
	
	public boolean colorsBefore(int j, int row) {
		for (int i = j; i >= 0; i--) {
			if (forbiddenColors[row][i] == forbiddenColors[row][j])
				return false;
		}
		return true;
	}
}