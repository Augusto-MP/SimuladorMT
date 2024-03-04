
package teoria.simuladormt;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TuringMachineSimulator {

    private static class StateTransition {
        int fromState;
        char readSymbol;
        int toState;
        char writeSymbol;
        char direction;

        public StateTransition(int fromState, char readSymbol, int toState, char writeSymbol, char direction) {
            this.fromState = fromState;
            this.readSymbol = readSymbol;
            this.toState = toState;
            this.writeSymbol = writeSymbol;
            this.direction = direction;
        }
    }

    private int currentState;
    private List<Integer> finalStates;
    private char whiteSymbol;
    private List<StateTransition> transitions;

    public TuringMachineSimulator(String jsonFilePath) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(jsonFilePath));

        this.currentState = Integer.parseInt(jsonObject.get("initial").toString());
        this.finalStates = (List<Integer>) jsonObject.get("final");
        this.whiteSymbol = ((String) jsonObject.get("white")).charAt(0);

        JSONArray transitionsArray = (JSONArray) jsonObject.get("transitions");
        this.transitions = new ArrayList<>();

        for (Object transitionObj : transitionsArray) {
            JSONObject transitionJson = (JSONObject) transitionObj;
            int fromState = Integer.parseInt(transitionJson.get("from").toString());
            int toState = Integer.parseInt(transitionJson.get("to").toString());
            char readSymbol = ((String) transitionJson.get("read")).charAt(0);
            char writeSymbol = ((String) transitionJson.get("write")).charAt(0);
            char direction = ((String) transitionJson.get("dir")).charAt(0);

            StateTransition transition = new StateTransition(fromState, readSymbol, toState, writeSymbol, direction);
            transitions.add(transition);
        }
    }

    public void simulate(String inputFilePath, String outputFilePath) throws IOException {
    FileWriter writer = new FileWriter(outputFilePath);

    FileReader reader = new FileReader(inputFilePath);
    int data;
    StringBuilder wordBuilder = new StringBuilder();
    while ((data = reader.read()) != -1) {
        char character = (char) data;
        if (character != '\n' && character != '\r') {
            wordBuilder.append(character);
        } else {
            String word = wordBuilder.toString().trim();
            if (!word.isEmpty()) {
                String result = simulateWord(word);
                writer.write(word + " - " + result + "\n");
            }
            wordBuilder = new StringBuilder();
        }
    }

    // Processa a última palavra se houver
    String lastWord = wordBuilder.toString().trim();
    if (!lastWord.isEmpty()) {
        String result = simulateWord(lastWord);
        writer.write(lastWord + " - " + result + "\n");
    }

    writer.close();
    reader.close();
}

    private String simulateWord(String word) {
        char[] tape = (word + whiteSymbol).toCharArray();
        int head = 0;

        while (!finalStates.contains(currentState)) {
            char currentSymbol = (head >= 0 && head < tape.length) ? tape[head] : whiteSymbol;

            boolean transitionFound = false;
            for (StateTransition transition : transitions) {
                if (transition.fromState == currentState && transition.readSymbol == currentSymbol) {
                    tape[head] = transition.writeSymbol;
                    if (transition.direction == 'R') {
                        head++;
                    } else {
                        head--;
                    }
                    currentState = transition.toState;
                    transitionFound = true;
                    break;
                }
            }

            if (!transitionFound) {
                return "Rejected";
            }
        }

        return "Accepted";
    }

    public static void main(String[] args) {
        try {
            TuringMachineSimulator simulator = new TuringMachineSimulator("specifications.json");
            simulator.simulate("words.txt", "results.txt");
            System.out.println("Simulation complete. Results written to results.txt");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}

