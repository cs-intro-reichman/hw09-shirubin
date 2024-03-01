import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class LanguageModel {


    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
        String window = "";
        char c;
        In in = new In(fileName);
        for (int i = 0; i < windowLength; i++) {
            window += in.readChar();
        }
        while (!in.isEmpty()) {
            c = in.readChar();
            List probs = CharDataMap.get(window);
            if(probs==null){
                probs = new List();
                CharDataMap.put(window, probs);
            }
            probs.update(c);
            window += c;
            window = window.substring(1);
        }
        
        for (List probs : CharDataMap.values()){
            calculateProbabilities(probs);
        } 
        // for (Map.Entry<String,List> entry: CharDataMap.entrySet()){

        //     System.out.println( entry.getKey() + "  " + entry.getValue());
        // } 
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {	
        ListIterator itr = probs.listIterator(0);	
        double counter = 0;
        while (itr.hasNext()) {   
            counter+=itr.current.cp.count;
            itr.next();
        }	
        ListIterator prev = probs.listIterator(0);	
        itr = probs.listIterator(1);	
        int probsSize = probs.getSize();
        if(probsSize==1){
            prev.current.cp.p = 1;
            prev.current.cp.cp = 1;
        }
        else{
            for (int i = 0; i < probsSize-1; i++) { 
                itr.current.cp.p=itr.current.cp.count/counter;
                if (i==0){
                    prev.current.cp.p=prev.current.cp.count/counter;
                    prev.current.cp.cp=prev.current.cp.p;
                    itr.current.cp.cp = prev.current.cp.cp + itr.current.cp.p; 
                }
                else{
                    itr.current.cp.cp = prev.current.cp.cp + itr.current.cp.p; 
                }
                prev.next();
                itr.next();
            }
        }
        
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
		// Your code goes here
        double r = randomGenerator.nextDouble();
        char randChar = ' ';
        ListIterator itr = probs.listIterator(0);	
        for (int i = 0; i < probs.getSize(); i++) {
            if (r <= itr.current.cp.cp){
                randChar = itr.current.cp.chr;
                return randChar;
            } 
            itr.next();
        }
        return randChar;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
        if(textLength<windowLength){
            return initialText;
        }
        String window = initialText.substring(initialText.length()-windowLength);
        while (textLength!=window.length()) {
            List probs = CharDataMap.get(window);
            if(probs==null){
                return initialText;
            }
            initialText += getRandomChar(probs);
            window = initialText.substring(initialText.length()-windowLength);
        }
        return initialText;
		// Your code goes here
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        // int windowLength = Integer.parseInt(args[0]);
        // String initialText = args[1];
        // int generatedTextLength = Integer.parseInt(args[2]);
        // Boolean randomGeneration = args[3].equals("random");
        // String fileName = args[4];
        int windowLength = 8;
        String initialText = "FENNYMAN";
        int generatedTextLength = 1000;
        Boolean randomGeneration = true;
        String fileName = "shakespeareinlove.txt";
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
        lm = new LanguageModel(windowLength);
        else
        lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        // System.out.println(lm.generate(initialText, generatedTextLength));
    }
}
