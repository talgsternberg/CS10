/**
 * Function that uses the viterbi method to predict
 */

import java.util.*;


public class Viterbi {
    // data structures for currstates and currscores
    private HashSet<String> currStates;
    private HashMap<String, Double> currScoreMap;
    // penalty in observation score for an unseen word
    private static final double unseenWordPenalty = -100;

    // the most likely final sequence of tags
    public List<String> sequence;

    // a map to track the sequences that lead to the tags in each step
    private ArrayList<HashMap<String, String>> backtrack;

    public static void main(String[] args) throws Exception {

        // ice cream map test
        HashMap<String, HashMap<String, Double>> ictm = new HashMap<>(); // ice cream transition map

        // build ice cream transitions
        HashMap<String, Double> hottransitions = new HashMap<>();
        hottransitions.put("hot", -0.3);
        hottransitions.put("cold", -0.3);

        HashMap<String, Double> coldtransitions = new HashMap<>();
        coldtransitions.put("hot", -0.7);
        coldtransitions.put("cold", -0.97);

        HashMap<String, Double> starttransitions = new HashMap<>();
        starttransitions.put("hot", -0.4);
        starttransitions.put("cold", -0.22);

        // put the transitions in the map
        ictm.put("hot", hottransitions);
        ictm.put("cold", coldtransitions);
        ictm.put("#", starttransitions);

        // build ice cream observations
        HashMap<String, HashMap<String, Double>> icom = new HashMap<>(); // ice cream observation map

        HashMap<String, Double> hotobservations = new HashMap<>();
        hotobservations.put("one cone", -0.6);
        hotobservations.put("two cones", -0.6);
        hotobservations.put("three cones", -0.3);

        HashMap<String, Double> coldobservations = new HashMap<>();
        coldobservations.put("one cone", -0.18);
        coldobservations.put("two cones", -0.77);
        coldobservations.put("three cones", -0.77);

        // put the observations in the map
        icom.put("hot", hotobservations);
        icom.put("cold", coldobservations);

        // our test observations that we'll run viterbi on
        String[] ice_cream_obs = new String[3];
        ice_cream_obs[0] = "two cones";
        ice_cream_obs[1] = "three cones";
        ice_cream_obs[2] = "two cones";

        // print out the sequence the computer predicts given those observations
        System.out.println(new Viterbi(icom, ictm, ice_cream_obs));


        // simple test from test files

        // train the computer
        HiddenMarkovModel simpletest = new HiddenMarkovModel();
        simpletest.processFiles("PS-5/simple-train-sentences.txt", "PS-5/simple-train-tags.txt");

        // print out the predicted tag sequence for each sentence in the test sentences
        ArrayList<String[]> simplesentences = simpletest.splitFile("PS-5/simple-test-sentences.txt");
        for (String[] sentence : simplesentences){
            System.out.println(new Viterbi(simpletest.observationMap, simpletest.transitionMap, sentence));
        }

        // tell us how accurate it was
        accuracy("PS-5/simple-train-tags.txt", "PS-5/simple-train-sentences.txt", "PS-5/simple-test-tags.txt", "PS-5/simple-test-sentences.txt");


        // Brown test

        // accuracy of brown test
        accuracy("PS-5/brown-train-tags.txt", "PS-5/brown-train-sentences.txt", "PS-5/brown-test-tags.txt", "PS-5/brown-test-sentences.txt");

    }

    // (constructor) function to run viterbi; takes two maps to train on and then a sentence to predict the tags for
    public Viterbi(HashMap<String, HashMap<String, Double>> observationMap, HashMap<String, HashMap<String, Double>> transitionMap, String[] subject){
        //currStates = { start } – our starting current states is always just the # sign
        currStates = new HashSet<String>();
        currStates.add("#");

        // instantiate backtrack and add an empty map to it
        backtrack = new ArrayList<HashMap<String, String>>();
        backtrack.add(new HashMap<>());


        //currScores = map { start=0 } – current score of the start is always 0
        currScoreMap = new HashMap<String, Double>();
        currScoreMap.put("#", 0.0);

        //for i from 0 to # observations - 1
        for (int i = 0; i < subject.length; i++) {
            // add map to backtrack
            backtrack.add(new HashMap<>());

            //nextStates = {} – empty set so no repeat states
            HashSet<String> nextStates = new HashSet<String>();

            //nextScores = empty map
            HashMap<String, Double> nextScoresMap = new HashMap<String, Double>();

            //for each currState in currStates
            for (String state : currStates) {
                //for each transition currState -> nextState
                if (transitionMap.get(state) != null) {
                    for (String nextState : transitionMap.get(state).keySet()) {
                        //add nextState to nextStates
                        nextStates.add(nextState);

                        // finding the observation score
                        double observationScore = 0.0;

                        // handle a word if it's unseen by giving it the unseen word penalty(-100)
                        if (observationMap.get(nextState).get(subject[i]) == null) {
                            observationScore = unseenWordPenalty;
                        } else {
                            observationScore = observationMap.get(nextState).get(subject[i]);
                        }

                        // handle an unseen tag
                        Double nextScore;
                        if (currScoreMap.get(state) == null) {
                            nextScore = -100.0;
                        } else {
                            nextScore = currScoreMap.get(state);
                        }

                        // calculate the next score
                        nextScore += transitionMap.get(state).get(nextState) + observationScore;


                        //if nextState isn't in nextScores or nextScore > nextScores[nextState]
                        if (!nextScoresMap.containsKey(nextState) || nextScore > nextScoresMap.get(nextState)) {
                            //set nextScores[nextState] to nextScore
                            //System.out.println(nextState + " " + state);
                            nextScoresMap.put(nextState, nextScore);
                            //remember that the predecessor of nextState @ i is curr
                            backtrack.get(i).put(nextState, state);
                        }
                    }
                }

            }
            // advance current states and current score map

            //currStates = nextStates
            currStates = nextStates;
            //currScores = nextScores
            currScoreMap = nextScoresMap;
        }
        // find most likely final state – uses an  iterator
        String finalState;
        Iterator<String> i = currStates.iterator();

        finalState = i.next(); // temporary final state

        // compare all the final tags in currentstate and choose the most likely one
        while (i.hasNext()) {
            String currState = i.next();
            if (currScoreMap.get(currState) > currScoreMap.get(finalState)){
                finalState = currState;
            }
        }

        // backtrace method on backtrack and finalstate to determine the most likely sequence
        sequence = backtrace(finalState, backtrack);
    }

    // method to build the most likely sequence given the most likely final state and backtrack which tracks predecessors
    public List<String> backtrace (String finalState, ArrayList<HashMap<String, String>> backtrack) {
        ArrayList<String> sequence = new ArrayList<>(); // arraylist that will be the sequence
        sequence.add(finalState); // add the final state

        // go through backtrack building the sequence by always inserting at the first index
        for (int i = backtrack.size() - 2; i > 0; i--) {
            sequence.add(0, backtrack.get(i).get(sequence.get(0)));
        }

        return sequence;
    }

    // method to determine how accurate our trained viterbi was given training files and test files
    public static void accuracy (String tagtrainingfile, String sentencetrainingfile, String tagtestfile, String sentencetestfile) throws Exception{
        // train the computer
        HiddenMarkovModel trained = new HiddenMarkovModel();
        trained.processFiles(sentencetrainingfile, tagtrainingfile);

        // trackers for how many tags we got correct and incorrect
        int correct = 0;
        int incorrect = 0;

        // arraylists so we can iterate through the correct tags and test sentences
        ArrayList<String[]> splittags = trained.splitFile(tagtestfile);
        ArrayList<String[]> splitsentences = trained.splitFile(sentencetestfile);

        // for loop to do the actual predictions and analysis
        for (int i = 0; i < splitsentences.size(); i++){
            // predict the tags for each sentence in the file
            Viterbi currtags = new Viterbi(trained.observationMap, trained.transitionMap, splitsentences.get(i));
            // get the actual tags for that  file
            String[] truetags = splittags.get(i);

            // compare if the tag we predicted is the same as the correct tag and increment incorrect or correct accordingly
            for (int t = 0; t < currtags.getSequence().size(); t++) {
                if (currtags.getSequence().get(t).equals(truetags[t])) correct++;
                else incorrect ++;
            }
        }

        // print out the results
        System.out.println("Running Viterbi on " + sentencetestfile + " trained with " + tagtrainingfile + " and " +
                sentencetrainingfile + " got " + correct + " tags correct and " + incorrect + " tags incorrect.");
    }

    // to string for viterbi that returns the sequence of a viterbi object
    @Override public String toString() {
        return sequence.toString();
    }

    // getter for the sequence
    public List<String> getSequence(){
        return sequence;
    }


}

