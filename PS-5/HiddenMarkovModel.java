/**
 * Hidden Markov Model class – trains the computer with a Hidden Markov Model to be able to try and predict states (tags) in provided sentences
 * Authors: Tal Sternberg and Jason Saber
 * 2/28/20
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class HiddenMarkovModel {
    // instance variables for the maps
    public HashMap<String, HashMap<String, Double>> transitionMap; // transition map – state that points to maps of different states that point to a transition score
    public HashMap<String, HashMap<String, Double>> observationMap; // map of observation probabilities – each state maps to a map of words and observation score

    public HiddenMarkovModel() throws IOException {
        // instantiate the maps
        transitionMap = new HashMap<String, HashMap<String, Double>>();
        observationMap = new HashMap<String, HashMap<String, Double>>();
    }

    // function to train based on user input
    public void processInput() throws Exception {
        // console test
        Scanner in = new Scanner(System.in);
        // get the sentence and put it in a file
        System.out.println("Please input a sentence in the form of <but he warmed up after a while .>");
        BufferedWriter insentences = new BufferedWriter(new FileWriter("PS-5/input-train-sentences.txt"));
        insentences.write(in.nextLine());

        // get the tags and put it in a file
        System.out.println("Please input the correct tags for that sentence in the form of <CNJ PRO VD ADV P DET N .>");
        BufferedWriter intags = new BufferedWriter(new FileWriter("PS-5/input-train-tags.txt"));
        intags.write(in.nextLine());

        // make hidden markov
        processFiles("PS-5/input-train-sentences.txt", "PS-5/input-train-tags.txt");
    }

    public void processFiles(String sentenceFileName, String tagFileName) throws Exception{
        // store the files in arraylists
        ArrayList<String[]> tagLines = splitFile(tagFileName);
        ArrayList<String[]> sentenceLines = splitFile(sentenceFileName);

        // for every tag sentence in the tag file
        for (int line = 0; line < tagLines.size(); line++) { //line: number of line im on
            String currentState = "#";
            // for every tag
            for (int i = 0; i < tagLines.get(line).length; i++) {//i: pos within line

                // nextstate for transitions
                String nextState = tagLines.get(line)[i];

                //Build up: TRANSITIONS

                // if the current tag is not a key in transition map yet
                if (!transitionMap.containsKey(currentState)) {
                    // create the inner map and add the tag after this one (it means this one can transition to that one) and give it a transition count of 1
                    HashMap<String, Double> innerMap = new HashMap<>();
                    innerMap.put(nextState, 1.0); // these counts are temporary – will be processed as probabilities -> logs later once the counts have been finalized
                    // put the current tag as a key and add the inner map
                    transitionMap.put(currentState, innerMap);
                }
                // if it already has the key
                else {
                    // if the inner map already has the next key in lines increment the count
                    if (transitionMap.get(currentState).containsKey(nextState)) {
                        double currCount = transitionMap.get(currentState).get(nextState);
                        transitionMap.get(currentState).put(nextState, currCount + 1); // increment the count of the transition
                    }
                    // if the inner map doesn't have the next key yet, add it to the inner map with a transition count of 1
                    else {
                        // put the next key and a transition count of 1 into the inner map
                        transitionMap.get(currentState).put(nextState, 1.0);
                    }
                }

                //Build up: OBSERVATIONS


                //if the current tag is not a key in the observation map yet
                if (!observationMap.containsKey(tagLines.get(line)[i])) {
                    //new inner map
                    HashMap<String, Double> innerMapOb = new HashMap<>();
                    innerMapOb.put(sentenceLines.get(line)[i], 1.0); // these counts are temporary – will be processed as probabilities -> logs later once the counts have been finalized
                    observationMap.put(tagLines.get(line)[i], innerMapOb);//the POS mapped to a map of words mapped to frequency
                } else {
                    // if the inner map already has the next key in lines increment the count
                    if (observationMap.get(tagLines.get(line)[i]).containsKey(sentenceLines.get(line)[i])) {
                        double currCount = observationMap.get(tagLines.get(line)[i]).get(sentenceLines.get(line)[i]);//get current count
                        observationMap.get(tagLines.get(line)[i]).put(sentenceLines.get(line)[i], currCount + 1); // increment the count of the transition
                    }
                    // if the inner map doesn't have the next key yet, add it to the inner map with a transition count of 1
                    else {
                        // put the next key and a transition count of 1 into the inner map
                        observationMap.get(tagLines.get(line)[i]).put(sentenceLines.get(line)[i], 1.0);//set count to 1.0
                    }
                }
                // advance current state
                currentState = nextState;
            }
            // deal with final tag – mostly same logic from above, just accounting for the fact that nothing follows
            // transition
            if (!transitionMap.containsKey(currentState)) {
                // create the inner map but is empty
                HashMap<String, Double> innerMap = new HashMap<>();
                // put the current tag as a key and add the inner map
                transitionMap.put(currentState, innerMap);
            }

            if (!observationMap.containsKey(currentState)) {
                //new inner map
                HashMap<String, Double> innerMapOb = new HashMap<>();
                innerMapOb.put(sentenceLines.get(line)[sentenceLines.get(line).length-1], 1.0); // these counts are temporary – will be processed as probabilities -> logs later once the counts have been finalized
                observationMap.put(currentState, innerMapOb);//the POS mapped to a map of words mapped to frequency
            } else {
                // if the inner map already has the final key in lines increment the count
                if (observationMap.get(currentState).containsKey(sentenceLines.get(line)[sentenceLines.get(line).length-1])) {
                    double currCount = observationMap.get(currentState).get(sentenceLines.get(line)[sentenceLines.get(line).length-1]);//get current count
                    observationMap.get(currentState).put(sentenceLines.get(line)[sentenceLines.get(line).length-1], currCount + 1); // increment the count of the transition
                }
                // if the inner map doesn't have the next key yet, add it to the inner map with a transition count of 1
                else {
                    // put the next key and a transition count of 1 into the inner map
                    observationMap.get(currentState).put(sentenceLines.get(line)[sentenceLines.get(line).length-1], 1.0);//set count to 1.0
                }
            }
        }

        // process the counts of the transition map
        for (String key : transitionMap.keySet()) {
            double transitionNum = 0; // the total number of transitions a specific tag will have
            // iterate through all the keys of the innner map
            for (String innerKey : transitionMap.get(key).keySet()) {
                transitionNum += transitionMap.get(key).get(innerKey); // add all the counts of every transition
            }
            // iterate through all the inner keys again, this time reassigning the counts to be the log of their probabilities
            for (String innerKey : transitionMap.get(key).keySet()) {
                // replace all the counts with log(transition count/total transitions)
                transitionMap.get(key).put(innerKey, Math.log(transitionMap.get(key).get(innerKey) / transitionNum));
            }
        }

        // process the inner maps of the observation map
        for(String key: observationMap.keySet()){
            double observationNum = 0;
            //iterate through inner map
            for(String innerKey: observationMap.get(key).keySet()){
                //increment my observationNum
                observationNum += observationMap.get(key).get(innerKey);
            }
            //iterate again and reassign values to log
            for(String innerKey: observationMap.get(key).keySet()){
                //replace counts
                observationMap.get(key).put(innerKey, Math.log(observationMap.get(key).get(innerKey) / observationNum));
            }
        }
    }


    //splits our files
    public ArrayList<String[]> splitFile(String fileName) throws IOException {
        ArrayList<String[]> splitFile = new ArrayList<String[]>();
        BufferedReader file = new BufferedReader(new FileReader(fileName));

        String line;
        while ((line = file.readLine()) != null) {
            splitFile.add(line.split(" "));
        }

        return splitFile;
    }

    //test it!
    public static void main(String[] args) throws Exception {

        //simple test
        HiddenMarkovModel test = new HiddenMarkovModel();
        test.processFiles("PS-5/simple-test-sentences.txt", "PS-5/simple-test-tags.txt");
        System.out.println(test.transitionMap);
        System.out.println(test.observationMap);
        String[] input = new String[4];
        input[0] = "the";
        input[1] = "dog";
        input[2] = "walked";
        input[3] = ".";
        Viterbi testVi = new Viterbi(test.observationMap, test.transitionMap, input);
        System.out.println(testVi.sequence);


        //simple train
        HiddenMarkovModel train = new HiddenMarkovModel();
        train.processFiles("PS-5/simple-train-sentences.txt", "PS-5/simple-train-tags.txt");

        ArrayList<String[]> tags = train.splitFile("PS-5/simple-test-tags.txt");
        ArrayList<String[]> sentences = train.splitFile("PS-5/simple-test-sentences.txt");


        // console test
        HiddenMarkovModel inputtest = new HiddenMarkovModel();
        inputtest.processInput();
        System.out.println(inputtest.transitionMap);
        System.out.println(inputtest.observationMap);

    }

}
