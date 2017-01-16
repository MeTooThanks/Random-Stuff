import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/*
	1. STILL NOT EXACT FOR BIPARTITE GRAPHS - BUG NOT FOUND YET
	2. PERFORMS WORSE FOR SOME GRAPHS THAN A CORRECTLY IMPLEMENTED DSATUR ALGORITHM
	3. SPEED ALREADY QUITE GOOD
*/

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
		
		double tempUB = quadraticEqSolver(edges);
		lb = 1;
		adjacencyMatrix = new int[vertices][vertices];
		removed = new boolean[vertices];
		adjacency();
		
		/*if (excludePlanarity()) {
			DSATURmain();
		}
		else {
			if (checkPlanarity())
				printUB(4);
			else
				DSATURmain();
		}*/
	}
	

	
	public boolean isInClique(int i, int k, ArrayList<Integer> cliqueSet) {
		int[] row = new int[vertices];
		for (int m = 0; m < cliqueSet.size(); m++) {
			row[cliqueSet.get(m)] = 1;
		}
		
		for (int p = 0; k < vertices; k++) {
			if (row[p] == 1 && adjacencyMatrix[k][p] == 0) {
				return false;
			}
		}
		return true;
	}
	
	public double quadraticEqSolver(int edges) {
		double resultOne;
		double resultTwo;
		
		resultOne = (0.5) + Math.sqrt(0.25+(2*edges));
		resultTwo = (0.5) - Math.sqrt(0.25+(2*edges));
		
		return Math.floor(Math.max(resultOne, resultTwo));
	}
	
	//creates the adjacency matrix
	public void adjacency() {
		for (ColEdge edge : graph) {
			adjacencyMatrix[edge.u-1][edge.v-1] = 1;
			adjacencyMatrix[edge.v-1][edge.u-1] = 1;
		}
		
		adjSimple();
	}
	
	//triangularizes the adjacency matrix
	public void adjSimple() {
		adjacencySimple = new int[vertices][vertices];
		for (int i = 1; i < vertices; i++) {
			for (int j = 0; j < i; j++) {
				adjacencySimple[i][j] = adjacencyMatrix[i][j];
			}
		}
	}
	
	//basically the same as with normal greedy coloring, only that the nodes aren't handled sequentially.
	//but choseVertex() decides which node will be handled
	public void DSATURmain() {
		forbiddenColors = new int[vertices][vertices];
		assignedColors = new int[vertices];
		int tempK = 0;
		boolean found;
		removedVertices = new ArrayList<Integer>();
		
		//is set for every row, which adjacent vertices have which color
		boolean[] usedColors = new boolean[vertices];
		usedColors[0] = true;
		int vertex = 0;
		
		createDSATMatrix();
		
		for (int m = 0; m < vertices; m++) {
			vertex = choseVertex();
			
			//System.out.println(vertices-m);
			
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
			updateDSATMatrix(vertex);
		}
		
		//final evaluation which colors have been used
		for (int i = 0; i < assignedColors.length; i ++) {
			usedColors[assignedColors[i]] = true;
			System.out.print(assignedColors[i] +" ");
		}
		
		//looks for the first unused color, the index-1 of that color is the upper bound
		for (int i = 1; i < usedColors.length; i++)
			if (usedColors[i] == true){
				ub++;
			}
		
		System.out.println("NEW BEST UPPER BOUND: " +ub);
		System.out.println("NEW BEST LOWER BOUND: " +lb);
		
	}
	
	public int choseVertex() {	
		//sorts according to saturation
		sort(1, DSAT);
		
		//this block looks for the vertex with the highest saturation which isn't already colored
		int index = 0;
		while (removed[DSAT[index][0]] == true) {
			index++;
		}
		
		//checks if the saturation value for the node chosen in the block before is the same
		//for the next node, if yes, the next vertex will be chosen according to the highest
		//degree of connectivity in that subset of vertices
		if (index+1 < DSAT.length && DSAT[index][1] == DSAT[index+1][1]) {
			int tempIndex = index;
			
			//WORKS (OKAYISH?)
			
			//looks for the start and end of the sub array which contains the vertices with the same
			//degree of saturation
			while (index+1 < DSAT.length && DSAT[index][1] == DSAT[index+1][1]) {
				index++;
			}
			int[][] subArray = new int[index-tempIndex+1][3];
			
			//copies this sub array into a new array
			for (int i = 0; i < subArray.length; i++)
				for (int j = 0; j < DSAT[0].length; j++) {
					subArray[i][j] = DSAT[i+tempIndex][j];
				} 
			
			//sorts the sub array according to degrees of connectivity
			sort(2, subArray);
			
			index = 0;
			while (removed[subArray[index][0]]) {
				index++;
			}
			
			//returns the vertex with the highest degree of connectivity
			return subArray[index][0];
			
		} else {
			//if the vertex with the highest degree of saturation is the only vertex with that DSat, that vertex
			//will be returned
			return DSAT[index][0];
		}
	}
		
	
	//sorts descending
	public void sort(int column, int[][] array) {
		Arrays.sort(array, new Comparator<int[]>() {
            @Override
            public int compare(final int[] first, final int[] second) {
            	  return Integer.compare(second[column], first[column]);
            }
        });
	}
	
	//creates the DSAT matrix and sets the degrees of connectivity
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
	
	//updates the degrees of saturation values (only for nodes adjacent to the one that was just colored)
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
	
	//checks if the color with which an adjacent node was just colored is already a color of another adjacent node
	public boolean checkIfColorIsAlreadyAdjacent(int index, int nodeBefore) {
		for (int i = 0; i < vertices; i++) {
			if (forbiddenColors[index][i] == assignedColors[nodeBefore] && i != nodeBefore)
				return false;
		}
		return true;
	}
	
	//checks if the color the algorithm tries to color a node with is already used by an adjacent node
	public boolean colorsBefore(int j, int row) {
		for (int i = j; i >= 0; i--) {
			if (forbiddenColors[row][i] == forbiddenColors[row][j])
				return false;
		}
		return true;
	}
	
	public boolean excludePlanarity() {
		return (edges > 3*vertices-6) ? true : false;
	}
	
	/*public boolean checkPlanarity() {
		
	}
	
	public void printBounds(int ub) {
		System.out.println(x);
	}*/
}