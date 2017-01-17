import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class DSATURfinished {
	private int ub, lb, cn, vertices, edges;
	private ColEdge[] graph;
	int[] assignedColors;
	private int[][] adjacencyMatrix, DSAT;
	int[][] forbiddenColors;
	int[][] saturation;
	boolean[] removed;

	//constructor
	public DSATURfinished(ColEdge[] e, int initVertices, int initEdges) {
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
	
	//for debugging purposes, checks if the calculated coloring is legal
	public void sanityCheck() {
		for (int i = 0; i < vertices; i++)
			for (int j = 0; j < vertices; j++)
				if (assignedColors[i] == forbiddenColors[i][j]) {
					System.out.println("illegal coloring at: " +i +" and " +j +" with color: " +assignedColors[i]);
				}
	}

	//creates the adjacency matrix
	public void adjacency() {
		for (ColEdge edge : graph) {
			adjacencyMatrix[edge.u-1][edge.v-1] = 1;
			adjacencyMatrix[edge.v-1][edge.u-1] = 1;
		}
		createDSATMatrix();
	}
	
	//greedy coloring algorithm
	public void DSATUR() {
		forbiddenColors = new int[vertices][vertices];
		assignedColors = new int[vertices];
		int k = 0;
		int vertex = 0;
		
		//is set for every row, specifies which adjacent vertices have which color
		boolean[] usedColors = new boolean[vertices];
		
		while (DSAT.length > 0) {
			vertex = choseVertex();
			
			//sets the colors of the adjacent nodes as used
			for (int j = 0; j < vertices; j++)
				usedColors[forbiddenColors[vertex][j]] = true; 
				
			//assigns the first unused color
			for (k = 1; k < vertices && usedColors[k-1]; k++)
				assignedColors[vertex] = k;
			
			//sets the used color for the just colored node as a forbidden color for every adjacent node
			//k-1 because a for-loop increments k first, then checks if the condition is still true, so the k before that has to be set as a color
			for (int row = 0; row < vertices; row++)
				forbiddenColors[row][vertex] = (adjacencyMatrix[row][vertex] == 1) ? k-1 : 0;
			
			//resets the matrix of used colors
			usedColors = new boolean[vertices];
			
			removed[vertex] = true;
			updateDSATMatrix(vertex);
			delete(vertex);
		}
		
		//final evaluation which colors have been used
		for (int i = 0; i < assignedColors.length; i ++)
			usedColors[assignedColors[i]] = true;
		
		//increases ub by one every time it encounters a used color
		for (int i = 1; i < usedColors.length; i++)
			if (usedColors[i] == true)
				ub++;
		
		//----DEBUGGING----
		/*for (int i = 0; i < assignedColors.length; i ++)
			System.out.print(+assignedColors[i] +" ");
		System.out.print("\n"); */
		
		System.out.println("NEW BEST UPPER BOUND: " +ub);
		System.out.println("NEW BEST LOWER BOUND: " +lb);
	}
	
	//returns the number of the vertex to be colored next
	public int choseVertex() {	
		sort(1, DSAT);
		int index = 0;
		if (index+1 < DSAT.length && DSAT[index][1] == DSAT[index+1][1]) {
			int tempIndex = index;
			
			while (index+1 < DSAT.length && DSAT[index][1] == DSAT[index+1][1]) 
				index++;
			
			int[][] subArray = new int[index-tempIndex+1][3];
			
			for (int i = 0; i < subArray.length; i++) 
				subArray[i]= DSAT[tempIndex+i];
				
			sort(2, subArray);
			return subArray[0][0];
		} 
		else {
			return DSAT[index][0];
		}
	}
		
	//deletes the vertex which was just colored from the DSAT-matrix 
	public void delete(int vertex) {
		int index = 0;
		int[][] tempDSAT = new int[DSAT.length-1][3];
		
		while (DSAT[index][0] != vertex) 
			index++;
		
		for (int i = 0; i < index; i++) 
			tempDSAT[i] = DSAT[i];
		
		for (int i = index+1; i < DSAT.length; i++) 
			tempDSAT[i-1] = DSAT[i];
		
		DSAT = tempDSAT;
	}
	
	//sorts int[][] array according to int column (descending)
	public void sort(int column, int[][] array) {
		Arrays.sort(array, new Comparator<int[]>() {
            @Override
            public int compare(final int[] first, final int[] second) {
            	  return Integer.compare(second[column], first[column]);
            }
        });
	}
	
	//creates the DSAT-matrix and fills is with the vertex numbers and the degrees of connectivity
	public void createDSATMatrix() {
		DSAT = new int[vertices][3];
		for (int i = 0; i < vertices; i++) {
			DSAT[i][0] = i;
			for (int j = 0; j < vertices; j++) {
				if (adjacencyMatrix[i][j] == 1)
					DSAT[i][2]++;
			}
		}
	}
	
	//updates the degrees of saturation-values of the nodes adjacent to nodeBefore (the just colored node)
	public void updateDSATMatrix(int nodeBefore) {
		for (int i = 0; i < vertices; i++) {
			if (removed[i] == false && adjacencyMatrix[nodeBefore][i] == 1 && checkIfColorIsNotAdjacent(i, nodeBefore)) {
				int index = 0;
				while (i != DSAT[index][0]) {
					index++;
				}
				DSAT[index][1]++;
			}
		}
	}
	
	//checks if the color with which the nodeBefore was just colored is already adjacent to the node for which the DSAT-value is being updated
	public boolean checkIfColorIsNotAdjacent(int index, int nodeBefore) {
		for (int i = 0; i < vertices; i++) {
			if (forbiddenColors[index][i] == assignedColors[nodeBefore] && i != nodeBefore)
				return false;
		}
		return true;
	}
}