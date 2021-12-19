import java.util.*;

public class BFSClass {
/**
 * BFSClass.java  which creates bfs functions to be implemented in the Bacon Game
 * Feb 2020
 * @author Tal Sternberg and Jason Saber
 */




    /**
     * Function to perform BFS and build a Tree path (represented with a graph to allow for more than 2 children)
     * @param g the Graph of actors as vertices and shared movies as edges
     * @param source the center of the universe
     * @return the Tree path graph
     */
    public static <V,E> Graph<V, E> bfs(Graph<V, E> g, V source) throws Exception {
        Graph<V, E> backTrack = new AdjacencyMapGraph<V, E>();//new graph
        backTrack.insertVertex(source);//insert root node

        Set<V> visited = new HashSet<V>();//set to see if visited vertex yet
        SimpleQueue<V> queue = new SLLQueue<V>();//new queue

        queue.enqueue(source);//enqueue root vertex

        while (!queue.isEmpty()) {
            V u = queue.dequeue();//dequeue value named u


            if (visited.add(u)) {//check if visited / add if necessary
                for (V v : g.outNeighbors(u)) {
                    if (!visited.contains(v)) {
                        queue.enqueue(v);
                    }
                    //if backtrack doesnt have it, insert v and e
                    if(!backTrack.hasVertex(v)) {
                        backTrack.insertVertex(v);
                        backTrack.insertDirected(v, u, g.getLabel(v, u));
                    }

                }
            }
        }
        return backTrack;
    }

    /**
     * gets the path as a list from one actor to center of universe
     * @param treePath
     * @param person
     * @param <V>
     * @param <E>
     * @return
     */
    public static <V,E> List<V> getPath(Graph<V, E> treePath, V person) {
        ArrayList<V> path = new ArrayList<>();//new array list
        path.add(person);//add actor
        V temp = person;//temp var
        if(!treePath.hasVertex(temp)){//if the path doesnt have this vertex
            System.out.println("No connection from " + temp + " to center of universe.");// print no connection
            return null;
        }
        while (treePath.outDegree(temp) == 1) {//while the outdegree is one
            for (V parentCxn : treePath.outNeighbors(temp)) {//iterate through neighbors of temp
                temp = parentCxn;//set to store
                path.add(temp);//add to path
            }
        }
        return path;
    }


    /**
     * Function to make set of vertices with no path to center of universe
     * todo this comment
     * @param graph
     * @param subGraph
     * @return
     */
    public static <V,E> Set<V> missingVerticies(Graph<V, E> graph , Graph<V, E> subGraph){
        Set<V> missingVertices = new HashSet<V>();//new set
        for(V actor: graph.vertices()){//iterate through verticies
            if(!subGraph.hasVertex(actor)){//if subgraph (bfs) doesnt have actor
                missingVertices.add(actor);//add to missing verticies
            }
        }
        return missingVertices;
    }

    /**
     * function to find average seperation between nodes: actual method helped by recurive helper below
     * @param tree
     * @param root
     * @return
     */
    //for average seperation DO IT RECURSIVLY AND NEED HELPER METHOD!!!
    public static <V,E> double averageSeparation(Graph<V, E> tree, V root) {
        double averageSeperation = 0; //average seperation initially 0

        return ((averageSeperationHelper(averageSeperation, tree, root)))/(double)tree.numVertices();//return average # verticies


    }


    /**
     * recursive helper method for averageSeperation()
     */
    public static <V,E> double averageSeperationHelper(double averageSeperationBuilder, Graph<V, E> tree, V root){
        double countOfNodes = averageSeperationBuilder;
        for(V neighbor: tree.inNeighbors(root)){
            //base case do nothing

            //reccursive case
            countOfNodes += averageSeperationHelper(averageSeperationBuilder+1, tree, neighbor);//recursivly do it to next node
        }

        return countOfNodes;
    }


    /**
     * builds a list of average seperation in order of least to greatest to compare most/least connections
     * @param subtree
     * @param <V>
     * @param <E>
     * @return
     * @throws Exception
     */
    public static <V,E> List<V> averageSeparationListBuilder(Graph<V, E> subtree) throws Exception{

        ArrayList asl = new ArrayList();//new list
        for (V vertex : subtree.vertices()) {//go through actors
            Graph<V,E> bfs = bfs(subtree, vertex);//call bfs

            double avgsep = averageSeparation(bfs, vertex);//make avg sep double
//            System.out.println(vertex + " : " + avgsep);
//            if (vertex.equals("Kevin Bacon")) System.out.println("Kevin Bacon: " + avgsep);
            asl.add(new ActorSeparation(vertex, avgsep));// add value to list in object type ActorSeparation --> if value is zero: no connection to any actor in map
        }

        asl.sort(Comparator.comparingDouble((ActorSeparation a) -> (Double) a.separation));//sort list
        return asl;//return list
    }
}

