//package A3;

/**
 * CP372 - F19 - Computer Networks - Assignment 3
 * 
 * Uses Dijksta algorithm to determine shortest paths to server and client nodes
 * from all other routers in the network
 *
 * @author Justin Harrott
 * @version 2019-03-26
 *
 */


import java.io.*;
import java.util.*;

///////////////////////////////////////////////////////////////////////////////////

/**
 * Link-State
 * 
 */
public class LinkState {

	public static void main(String[] args) throws IOException {
		Graph_Set gs = new Graph_Set(args[0]);
//		Graph_Set gs = new Graph_Set("test6.txt");

		Routing_Table rt = new Routing_Table(gs);
//		System.out.println("**********print rt*************");
		rt.printRTables();

	}
}

///////////////////////////////////////////////////////////////////////////////////

/**
 * used by LinkSate to make a Graph object containing degree of matrix 2D array
 * from a .txt file that represents a graph of nodes
 * 
 * @param {String} file variable name of .txt file with graph data
 * @format line 0: n = size of square matrix lines 1 to n: are rows of square
 *         matrix, containing directed edge weights line n+1: are expected
 *         destination nodes represented in the table
 *
 */
class Graph_Set {
	int degree;
	int[][] matrix;
	int[] destinations;

	Graph_Set(String fv) throws IOException {
		// Open the data file for reading.
		final Scanner scnr = new Scanner(new File(fv));

		// Read the contents of the data file.
		degree = Integer.parseInt(scnr.nextLine().trim());
//		System.out.println(degree);

		// create 2D array for square matrix
//		matrix = new int[degree][degree];

		// Get each row of text file that pertains to graph edges and store in matrix
//		for (int i = 0; i < degree; i++) {
//			String[] vertex = scnr.nextLine().trim().split(" ");
//
//			// convert Strings of edge weights to int and store in matrix and
//			// graph
//			for (int j = 0; j < degree; j++)
//				matrix[i][j] = Integer.parseInt(vertex[j]);
//				if (matrix[i][j] == -1)  matrix[i][j] = 99999;
//		}
		// create 2D array for square matrix
		int[][] adjMatrix = new int[degree][degree];

		// Get each row of text file that pertains to graph edges and store in matrix
		for (int i = 0; i < degree; i++) {
			String[] row = scnr.nextLine().split(" ");
			for (int j = 0; j < degree; j++) {
				adjMatrix[i][j] = Integer.parseInt(row[j]);
				if (adjMatrix[i][j] == -1)
					adjMatrix[i][j] = 99999;
			}
		}
		matrix = adjMatrix;

		// get and store destination nodes
		String[] destStr = scnr.nextLine().trim().split(" ");
		destinations = new int[destStr.length];
		for (int i = 0; i < destStr.length; i++)
			destinations[i] = Integer.parseInt(destStr[i]);

		scnr.close();
		return;
	}
}

///////////////////////////////////////////////////////////////////////////////////

/**
 * Routing Table object. Is a collection of Forwarding Tables.
 * 
 * @param {Graph_Set} Containing elements required to make Routing Table
 *
 */
class Routing_Table {
	List<Dijkstra> shortestPaths = new ArrayList<>(); // Array of Fwd_Table objects

	Routing_Table(Graph_Set gs) {
		int j = 0;
		for (int i = 0; i < gs.degree; i++) {
			if (j < gs.destinations.length) {
				if (gs.destinations[j] == i + 1) {
					// System.out.println("to loop i=" + i+" j=" +j+ " rt="+ routersArr[j] );
					++j;
					continue;
				}
			}
			// System.out.println("to djik i=" + i+" j=" +j+ " rt="+ routersArr[j] );
			Dijkstra paths = new Dijkstra(gs, i);
			shortestPaths.add(paths);
		}
	}

	void printRTables() {
		for (Dijkstra paths : this.shortestPaths) {
		int[] servers = paths.servers;
		for (int destinationIndex = 0; destinationIndex < servers.length; destinationIndex++) {
			int destinationServer = servers[destinationIndex];
			if(destinationIndex == 0)
					System.out.println(String.format("\nForwarding table for %d\n%-5s%-8s%-8s", paths.startVertex, "To", "Cost", "Next Hop"));
			System.out.println(paths.toString(destinationServer));
			}
		}
	}
}

	/**
 * Function that implements Dijkstra's using adjacency matrix to create an
 * object containing vertex ID of starting node and shortest path to all other
 * vertices, corresponding array of previous vertices for shortest trip and
 * array of vertex IDs for destination servers
 * 
 * @param {}
 *
 */
class Dijkstra {
	int startVertex; // Vertex ID of starting node
	int[] shortestDistances;
	int[] previousNodes;
	int[] servers;

	private static final int NO_ADJACENCY = -1;
	private static final int INFINITY = Integer.MAX_VALUE;

	Dijkstra(Graph_Set gs, int startIndex) {
		startVertex = startIndex + 1;
		servers = gs.destinations;
		int degree = gs.degree;
		int[][] matrix = gs.matrix;
		shortestDistances = new int[degree];

		// array of completed vertices
		// true if vertex has been found to be the least cost from previous vertex and
		// has had
		// array of shortestDistances to adjacent vertices made
		boolean[] complete = new boolean[degree];

		// Iniatialize a new list of costs to adjacent vertices with infinity values
		for (int vertexIndex = 0; vertexIndex < degree; vertexIndex++) {
			shortestDistances[vertexIndex] = INFINITY;
			complete[vertexIndex] = false;
		}
	
		// Distance of source vertex from itself is always 0
		shortestDistances[startIndex] = 0;

		// array of previous vertex IDs pertaining to the least expensive costs
		previousNodes = new int[degree];

		previousNodes[startIndex] = NO_ADJACENCY;

		// Find shortest path to all vertices from the startVertex
		// first iteration will find that the startVertex has the least cost, as it will
		// be 0 then it will find the shortest path from it to its adjacent vertices,
		// then the shortest path from that one and so on
		for (int i = 1; i < degree; i++) {
	
			// find nearest adjacent vertex with the least cost for the trip
			int nearestVertex = -1;
			int shortestDistance = INFINITY;

			for (int vertexIndex = 0; vertexIndex < degree; vertexIndex++) {
				if (!complete[vertexIndex] && shortestDistances[vertexIndex] < shortestDistance) {
					nearestVertex = vertexIndex;
					shortestDistance = shortestDistances[vertexIndex];
				}
			}

	
			// create list of costs to all adjacent vertices to the vertex that was found to
			// have the shortest distance to travel
			for (int vertexIndex = 0; vertexIndex < degree; vertexIndex++) {
				int edgeDistance = matrix[nearestVertex][vertexIndex];
	
				if (edgeDistance > 0 && ((shortestDistance + edgeDistance) < shortestDistances[vertexIndex])) {
					previousNodes[vertexIndex] = nearestVertex;
					shortestDistances[vertexIndex] = shortestDistance + edgeDistance;
					}
			}

			// add nearest vertex to list of completed vertices
			complete[nearestVertex] = true;
		}
		return;
	}

	public String toString(int destinationServerID) {
		String s = new String();
		int vertexIndex = destinationServerID - 1;

		if (this.shortestDistances[vertexIndex] >= 99999)
			s = String.format("  %-5d  %-8d  %-6d", destinationServerID, -1, -1);
		else {
			if (this.previousNodes[vertexIndex] == NO_ADJACENCY)
				s = String.format("  %-5d  %-8d  %-6d", destinationServerID, this.shortestDistances[vertexIndex], -1);
			else {
				int lastVert = vertexIndex;

				while (this.previousNodes[vertexIndex] != NO_ADJACENCY) {
					lastVert = vertexIndex;
					vertexIndex = this.previousNodes[vertexIndex];
				}
				s = String.format("  %-5d  %-8d  %-6d", destinationServerID,
						this.shortestDistances[destinationServerID - 1], lastVert + 1);
			}
		}

		return s;
	}
}
