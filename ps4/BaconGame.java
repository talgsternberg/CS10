import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;



public class BaconGame {
    /**
     * BaconGame.java reads through files and creates maps and graphs to then perform BFS methods in BFSClass
     * Feb 2020
     * @author Tal Sternberg and Jason Saber
     */


//connected to GraphLib from SA-7

//class that uses my files to build maps and graphs necessary for the Bacon Game

    //declare my maps
    Map<String, String> actorMap;//map of actor ids to actor names
    Map<String, String> movieMap;//map of movie ids to movie names
    Map<String, ArrayList<String>> actorInMovieMap;//map of movie ids to list of actor ids

    //declare actor movie graph
    Graph<String, Set<String>> actorAppearInMovie;//graph w vert: actors connect actors in same movie w edge label of movie they were in


    public BaconGame(String actorFile, String movieFile, String actorInMovieFile)throws Exception {
        //instantiate my maps
        actorMap = new HashMap<String,String>();
        movieMap = new HashMap<String,String>();
        actorInMovieMap = new HashMap<String,ArrayList<String>>();


        //instantiate actor movie graph
        actorAppearInMovie = new AdjacencyMapGraph<String, Set<String>>();


        //set my maps
        Map<String, String> movieMap = readActorAndMovie(movieFile);//build movieMap
        Map<String, String> actorMap = readActorAndMovie(actorFile);//build actor map
        Map<String, ArrayList<String>> movieActorMap = readActorsInMovie(actorInMovieFile);//builds movies connected to actor map (not used but could potentially be helpful in other situations)

        //set my graph
        Graph<String, Set<String>> actorAppearInMovie = buildGraph(actorMap, movieMap, actorInMovieMap);

        //tell user the commands
        System.out.println("Commands:\n" +
                "c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation\n" +
                "d <low> <high>: list actors sorted by degree, with degree between low and high\n" +
                "i: list actors with infinite separation from the current center\n" +
                "p <name>: find path from <name> to current center of the universe\n" +
                "s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high\n" +
                "u <name>: make <name> the center of the universe\n" +
                "q: quit game\n");



        //input handling
        Scanner kb = new Scanner(System.in);
        String input = "";

        // get the center of the universe and build the average separation tree
        String centerofUniverse = "";
        while (!actorAppearInMovie.hasVertex(centerofUniverse)){
            System.out.println("Please input a valid center of the universe");
            centerofUniverse = kb.nextLine();
        }

        // set up treepath, center of universe's average separation, and avg separation list with initial game settings
        Graph<String, Set<String>> bfs = BFSClass.bfs(actorAppearInMovie, centerofUniverse);
        double averageSeparation = BFSClass.averageSeparation(bfs, centerofUniverse);
        List aseplist = BFSClass.averageSeparationListBuilder(bfs);

        //print out default game settings
        System.out.println(centerofUniverse + " is now the center of the acting universe, connected to " + actorAppearInMovie.outDegree(centerofUniverse) + "/9235 actors " +
                "with average separation " + averageSeparation + "\n");

        //print out the graphs
//        System.out.println("Actor vertex, movie edge graph: " + actorAppearInMovie);
//        System.out.println("Path graph: " + bfs);

        // local class to efficiently associate an actor name and their degree
        class ActorDegree{
            String name;
            int degree;
            ActorDegree(String name, int degree){
                this.name = name;
                this.degree = degree;
            }
            @Override
            public String toString(){
                return name + ":" + degree;
            }
        }

        // while the user has not asked to quit the game, play the game
        while (!input.equals("q")) {

            // print out game name + get input
            System.out.println("Kevin Bacon game >");
            input = kb.nextLine(); // get input each loop


            // if statements to handle certain inputs

            // c – list top # centers of the universe handler
            if (input.charAt(0) == 'c') {
                int number = 0; // default

                // handle bad input
                try {
                    String num = input.substring(2);
                    number = Integer.parseInt(num);
                    aseplist.subList(0, Math.abs(number)); // just to test if it's out of bounds
                }
                catch (Exception e){
                    // force them to give a good input
                    while(true) {
                        System.out.println("Please input a valid number.");
                        try {
                            number = Integer.parseInt(input);
                            break;
                        }
                        catch (Exception e1) {
                            input = kb.nextLine();
                        }
                    }
                }

                if (number > 0) {
                    // print out the # least connected actors
                    System.out.println("Number 0.0 means not in a movie with any other actor in graph.");
                    System.out.println("The " + number + " most connected centers of universe: " + aseplist.subList(0, number));
                    System.out.println("Kevin Bacon's Number: " + BFSClass.averageSeparation(bfs, "Kevin Bacon"));
                }
                else {
                    // find and reverse the sublist of the # most connected (so it lists most connected first) and then print
                    List most = aseplist.subList(aseplist.size() - Math.abs(number), aseplist.size());
                    Collections.reverse(most);
                    System.out.println("The " + Math.abs(number) + " least connected centers of universe: " + most);
                }

            }

            // d – print a bounded list of actors sorted by degree
            else if (input.charAt(0) == 'd') {
                // will only accept input accurately formatted with spaces separating the inputs
                String[] inputs = input.split(" ");

                // store bounds
                int low;
                int high;

                // handle bad input
                try {
                    low = Integer.parseInt(inputs[1]);
                    high = Integer.parseInt((inputs[2]));
                }
                catch (Exception e) {
                    // again keep them in while loop until they give valid input
                    while(true) {
                        System.out.println("Please input valid bounds in the form of <low high>");
                        try {
                            inputs = input.split(" ");
                            low = Integer.parseInt(inputs[0]);
                            high = Integer.parseInt(inputs[1]);
                            break;
                        }
                        catch (Exception e1) {
                            input = kb.nextLine();
                        }
                    }
                }

                // build a list of all the actors associated with their degrees
                List<ActorDegree> actorsWithMostDegrees = new ArrayList<ActorDegree>();
                for(String actor: actorAppearInMovie.vertices()){
                    ActorDegree ad = new ActorDegree(actor, actorAppearInMovie.outDegree(actor));
                    // if their degrees don't fit in the bounds don't add them
                    if (ad.degree >= low && ad.degree <= high){
                        actorsWithMostDegrees.add(ad);
                    }
                }
                // sort the list by degree and print it
                actorsWithMostDegrees.sort(Comparator.comparingInt((ActorDegree a) -> a.degree));
                System.out.println("Actors with least to most out degrees: " + actorsWithMostDegrees);
            }

            // i – list the actors who have no connection (infinite separation) to the center of the universe
            else if (input.charAt(0) == 'i') {
                System.out.println("The missing actors are " + BFSClass.missingVerticies(actorAppearInMovie, bfs));
            }

            // p – print the path from an inputted actor to the center of the universe
            else if (input.charAt(0) == 'p') {
                String currentActor = "";

                // handle bad input
                try {
                    currentActor = input.substring(2);
                }
                catch (Exception e){
                    System.out.println("Unable to process that input.");
                }

                // keep them in while loop until they give valid input
                while (!actorAppearInMovie.hasVertex(currentActor)){
                    System.out.println("Please input a valid actor name.");
                    currentActor = kb.nextLine();
                }

                // get the path
                List<String> path = BFSClass.getPath(bfs, currentActor);

                // print out the path
                if (path != null) {
                    System.out.println(currentActor + "'s number is " + (path.size() - 1));

                    for (String vertex : path){
                        for (String parent : bfs.outNeighbors(vertex)){
                            System.out.println(vertex + " appeared in " + bfs.getLabel(vertex, parent) + " in " + parent);
                        }
                    }
                }
            }

            // s –
            else if (input.charAt(0) == 's') {
                // will only accept input accurately formatted with spaces separating the inputs
                String[] inputs;//new outputs
                int low;//lowest degree
                int high;//highest degree
                try {
                    inputs = input.split(" ");//split the input
                    low = Integer.parseInt(inputs[1]);//parse for low
                    high = Integer.parseInt((inputs[2]));//parse for high
                }
                catch (Exception e) {
                    // keep in while loop until they give valid inputs
                    while(true) {//if the exception
                        System.out.println("Please input valid bounds in the form of <low high>");//ask for new input
                        try {
                            //do same conversion again
                            inputs = input.split(" ");
                            low = Integer.parseInt(inputs[0]);
                            high = Integer.parseInt(inputs[1]);
                            break;
                        }
                        catch (Exception e1) {
                            input = kb.nextLine();
                        }
                    }
                }

                // build list of actors associated with their separation
                List<ActorDegree> actorsInOrderOfDegree = new ArrayList<ActorDegree>();
                for(String actor: bfs.vertices()){
                    List<String> listPath = BFSClass.getPath(bfs, actor);
                    int degree = listPath.size()-1;
                    // make sure they're only added to the list if their separation from the center of the universe is within the bounds
                    if (degree >= low && degree <= high){
                        actorsInOrderOfDegree.add(new ActorDegree(actor, degree));
                    }
                }

                // sort and the list by separation
                actorsInOrderOfDegree.sort(Comparator.comparingInt((ActorDegree a) -> a.degree));
                System.out.println("Actors with smallest to biggest paths to center: " + actorsInOrderOfDegree);
            }

            // u – to redefine the center of the universe
            else if (input.charAt(0) == 'u') {
                centerofUniverse = "";
                // handle bad inputs
                try {
                    centerofUniverse = input.substring(2);
                }
                catch (Exception e){
                    System.out.println("Unable to process that input;");
                }
                // while loop to make sure they input valid input
                while (!actorAppearInMovie.hasVertex(centerofUniverse)){
                    System.out.println("Please input a valid center of the universe");
                    centerofUniverse = kb.nextLine();
                }

                // let the user know it's been updated and update the center of universe, bst, averageSeparation, and aseplist
                bfs = BFSClass.bfs(actorAppearInMovie, centerofUniverse);
                averageSeparation = BFSClass.averageSeparation(bfs, centerofUniverse);
                aseplist = BFSClass.averageSeparationListBuilder(bfs);
                System.out.println(centerofUniverse + " is now the center of the acting universe, connected to " + actorAppearInMovie.outDegree(centerofUniverse) + "/9235 actors " +
                        "with average separation " + averageSeparation + "\n");
            }

            // q – end the program
            else if(input.equals("q")) {
                System.out.println("Game ended.");
            }

            // if the command does not start with one of the valid command
            else {
                System.out.println("Sorry, that is not a valid command.");
            }
        }



    }

    //read in actor and movie files (same structure of id key name value)
    public Map<String,String> readActorAndMovie(String fileName) throws IOException{
        Map<String, String> genericMap = new HashMap<String,String>();//later set other maps equal

        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(fileName));
        }
        catch (Exception e) {
            System.out.println("Could not open file.");
        }


        try {
            String line; //each line will get held as read in
            while ((line = input.readLine()) != null) { //read each line
                String[] splitLine = line.split("\\|");//string array of split line
                genericMap.put(splitLine[0], splitLine[1]);//right side and left side
            }
        }
        catch(IOException e){
            System.out.println("error");

        }
        finally{
            input.close();//close
        }
        return genericMap;
    }

    //read in actor in movie file
    public Map<String, ArrayList<String>> readActorsInMovie(String fileName) throws IOException{
        //key is movie, value is actors in movie (arraylist)
        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(fileName));
        }
        catch (Exception e) {
            System.out.println("Could not open file.");
        }

        try {
            String line;
            while ((line = input.readLine()) != null) { //read each line
                String[] splitLine = line.split("\\|");//string array of split line
                //if it doesnt have key
                if (!actorInMovieMap.containsKey(splitLine[0])) {
                    ArrayList<String> actorsInFilm = new ArrayList<String>();
                    actorsInFilm.add(splitLine[1]);
                    actorInMovieMap.put(splitLine[0], actorsInFilm);//right side = movie, left=  id of actors list who act in movie
                }
                //if it has key, add to actor list of values
                else{
                    actorInMovieMap.get(splitLine[0]).add(splitLine[1]);
                }
            }
        }
        catch(IOException e){
            System.out.println("error");

        }
        finally{
            input.close();
        }
        return actorInMovieMap;

    }

    //build a graph w v=actors e=movies UNDIRECTED
    public Graph<String, Set<String>> buildGraph(Map<String, String> actorMap, Map<String, String> movieMap, Map<String, ArrayList<String>> actorInMovieMap){
        //iterate through actors and add actors to verticies
        for(Map.Entry<String, String> actorVertex: actorMap.entrySet()) {
            String actorId = actorVertex.getKey();
            actorAppearInMovie.insertVertex(actorMap.get(actorId));//add actor's name as a vertex
        }

        //for the movie ids and value list of actor ids
        for(String connectedMovies: actorInMovieMap.keySet()){//look through movie ids
            for(String actor: actorInMovieMap.get(connectedMovies)){//look through actors

                for(String connections: actorInMovieMap.get(connectedMovies)) {//loop through actors again to match actors to actors in same movies
                    if (connections != actor) {//make sure to not connect actor with themselves
                        if (!actorAppearInMovie.hasEdge(actorMap.get(actor), actorMap.get(connections))) {//if there is no edge
                            actorAppearInMovie.insertUndirected(actorMap.get(actor), actorMap.get(connections), new HashSet<String>());//make a new edge with empty set label
                        }
                        actorAppearInMovie.getLabel(actorMap.get(actor), actorMap.get(connections)).add(movieMap.get(connectedMovies));//fill set with connected movies for edge labels
                    }

                }
            }
        }

        return actorAppearInMovie;//return my graph
    }

    //small driver to test
    public static void main(String[] args) throws Exception, IOException {
        //new object
//        BaconGame test1 = new BaconGame("ps4/actorsTest.txt","ps4/moviesTest.txt", "ps4/movie-actorsTest.txt" );
        BaconGame test = new BaconGame("ps4/actors.txt","ps4/movies.txt", "ps4/movie-actors.txt" );


    }

}





