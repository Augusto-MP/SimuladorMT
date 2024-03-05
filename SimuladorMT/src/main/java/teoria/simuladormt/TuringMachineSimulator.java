package teoria.simuladormt;


import java.io.BufferedReader;
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

    private int initialState;
    private List<Integer> finalStates;
    private char whiteSymbol;
    private List<StateTransition> transitions;

    public TuringMachineSimulator(String jsonFilePath) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(jsonFilePath));

        this.initialState = Integer.parseInt(jsonObject.get("initial").toString());
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
        try (FileWriter writer = new FileWriter(outputFilePath);
             BufferedReader reader = new BufferedReader(new FileReader(inputFilePath))) {

            String word;
            while ((word = reader.readLine()) != null) {
                word = word.trim();
                if (!word.isEmpty()) {
                    String result = simulateWord(word);
                    writer.write(word + " - " + result + "\n");
                }
            }
        }
    }

   private String simulateWord(String word) {
    char[] tape = (word + whiteSymbol).toCharArray(); // Adiciona o símbolo branco à fita após a palavra
    int head = 0;
    int currentState = initialState; // Reinicia o estado atual para o estado inicial

    while (true) {
        char currentSymbol = (head >= 0 && head < tape.length) ? tape[head] : whiteSymbol;
        boolean transitionFound = false;

        // Verifica todas as transições possíveis para o estado atual e símbolo atual
        for (StateTransition transition : transitions) {
            if (transition.fromState == currentState && transition.readSymbol == currentSymbol) {
                tape[head] = transition.writeSymbol; // Escreve o símbolo de acordo com a transição
                head += (transition.direction == 'R') ? 1 : -1; // Move a cabeça de acordo com a direção
                currentState = transition.toState; // Atualiza o estado atual
                transitionFound = true;

                // Log das informações da transição aplicada
                System.out.println("Transition applied: fromState=" + transition.fromState +
                        ", readSymbol=" + transition.readSymbol + ", toState=" + transition.toState +
                        ", writeSymbol=" + transition.writeSymbol + ", direction=" + transition.direction +
                        ", currentState=" + currentState + ", currentSymbol=" + currentSymbol +
                        ", head=" + head);

                break;
            }
        }

        // Se nenhuma transição for encontrada, a palavra é rejeitada
        if (!transitionFound) {
            break; // Sai do loop while
        }
    }

    // Verifica se o estado atual é um estado final após o processamento da palavra
    if (finalStates.contains(currentState)) {
        return "Accepted"; // Palavra aceita
    } else {
        return "Rejected"; // Palavra rejeitada
    }
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

