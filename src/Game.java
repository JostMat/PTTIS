import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Game {

  Participant participant = new Participant();
  JSONArray participantsJSON = new JSONArray();
  int aiScore = 0;
  Scanner scanner = new Scanner(new InputStreamReader(System.in));
  ASCIIArtGenerator artGen = new ASCIIArtGenerator();
  Map<Integer, Integer[]> conditionOrderMap = Map.of(
          0, new Integer[]{0, 1,2},
          1, new Integer[]{0, 2,1},
          2, new Integer[]{1, 0,2},
          3, new Integer[]{1, 2,0},
          4, new Integer[]{2, 0,1},
          5, new Integer[]{2, 1,0}
          );
  int[] numberOfRounds = new int[]{2,2,2};


  public static void main(String[] args) throws Exception {
    Game game = new Game();

    game.loadDatabaseAndSetConditionOrder();

    game.displayIntro();

    game.obtainPersonData();

    for (int i = 0; i < 3; i++) {
      game.executeMatch();
    }

    game.saveLogToDatabase();
  }

  public void saveLogToDatabase() throws IOException {
    File f = new File("database.json");
    JSONObject database = new JSONObject();

    if (f.exists()) {
      OutputStream os = new FileOutputStream(f);
      participantsJSON.put(participant.createJSONEntry());
      database.put("participants", participantsJSON);
      byte[] participantsJSONBytes = database.toString(4).getBytes();
      os.write(participantsJSONBytes);
      System.out.println("Saved log to file!");
    } else {
      System.out.println("No database found to save log to!");
    }
  }

  public void loadDatabaseAndSetConditionOrder() throws URISyntaxException, IOException {

    File f = new File("database.json");
    JSONObject database;
    JSONArray participants;

    if (f.exists() && (f.length() > 2)) {
      InputStream is = new FileInputStream("database.json");
      String jsonTxt = IOUtils.toString(is, "UTF-8");
      System.out.println(jsonTxt);
      database = new JSONObject(jsonTxt);
      participantsJSON = database.getJSONArray("participants");

    } else {
      f.createNewFile();
      participants = new JSONArray();
      participant.setConditionOrder(Arrays.asList(conditionOrderMap.get(participants.length()).clone()));
    }


    //we can use this later to analyze the results as well
    participant.setConditionOrder(Arrays.asList(conditionOrderMap.get(participantsJSON.length()).clone()));

  }

  public void displayIntro() throws Exception {
    System.out.println();
    artGen.printTextArt("PTIIS Trust Game", ASCIIArtGenerator.ART_SIZE_SMALL,
            ASCIIArtGenerator.ASCIIArtFont.ART_FONT_MONO, "@");
    System.out.println();

    System.out.print("Default text to explain the game!\n");
  }

  public void obtainPersonData() {

    System.out.println("Default text to prompt for personal details!");
    System.out.println("Please enter your player name:");
    String name = scanner.nextLine();
    System.out.println(name);

    System.out.println("Please enter your age:");
    int age = Integer.valueOf(scanner.nextLine());

    System.out.println("Please enter your gender (m/f/d):");
    String gender = scanner.nextLine();
    while (! (gender.equals("m") || gender.equals("f") || gender.equals("d"))) {
      System.out.print(String.format("Unsupported letter %s. Please only use m, f or d)\n",
              gender));
      System.out.println("Please enter your gender (m/f/d):");
      gender = scanner.nextLine();
    }
    participant.setAge(age);
    participant.setName(name);
    participant.setGender(gender);
  }

  public void executeMatch() {
    boolean previousPlayerMove;
    boolean playerMove = true;
    boolean aiMove;
    int condition = participant.getNextCondition();

    System.out.print("Are you ready to play the game? (y/n)\n");
    while(! (scanner.nextLine().equals("y"))) {
      System.out.print("Are you ready to play the game? (y/n)\n");
    }

    System.out.println(String.format("Welcome %s to match %d", participant.getName(),
            participant.getMatchCounter()));

    for (int roundCounter = 0; roundCounter < numberOfRounds[condition]; roundCounter++) {
      System.out.println("Do you want to play fair (f) or cheat (c)?");
      previousPlayerMove = playerMove;
      playerMove = scanner.nextLine().equals("f");

      // fair == true

      switch (condition) {
        case 0:
          aiMove = roundCounter != 5;
          break;
        case 1:
          aiMove = roundCounter > 5;
          break;
        case 2:
          aiMove = previousPlayerMove;
          break;
        default:
          System.out.println("Invalid condition value");
          return;
      }

      //compute the result
      if (playerMove && aiMove){
        participant.increaseScore(2);
        aiScore += 2;
        System.out.println("Both parties played fair.");
      } else if (playerMove) {
        aiScore += 3;
        System.out.println("The AI cheated.");
      } else if (aiMove) {
        participant.increaseScore(3);
        System.out.println("The AI played fair.");
      } else {
        System.out.println("Both cheated.");
      }
      participant.addDecision(playerMove);
      System.out.println(String.format("The new scores are:\n Human: %d     AI: %d",
              participant.getScore(), aiScore));
    }

    if (participant.getScore() > aiScore) {
      System.out.println("You won the game!");
    } else {
      System.out.println("You lost the game!");
    }

    participant.resetScore();
    participant.incrementMatchCounter();
    aiScore = 0;
  }


  public class Participant {
    int age;
    String gender;
    String name;
    int matchCounter = 0;
    int score;
    List<List<Boolean>> decisions = new ArrayList<>();

    List<Integer> conditionOrder = new ArrayList<Integer>();

    public Participant(){
      decisions.add(new ArrayList<>());
      decisions.add(new ArrayList<>());
      decisions.add(new ArrayList<>());
    }

    public void setConditionOrder(List<Integer> conditionOrder) {this.conditionOrder =
            conditionOrder;}
    public int getMatchCounter() {return this.matchCounter;}
    public void increaseScore(int increaseValue) {this.score += increaseValue;}
    public void addDecision(boolean decision) {this.decisions.get(matchCounter).add(decision);}

    public void setAge(int age) {
      this.age = age;
    }

    public void setGender(String gender) {
      this.gender = gender;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {return this.name;}
    public int getAge() {return this.age;}
    public int getScore() {return this.score;}
    public String getGender() {return this.gender;}
    public JSONObject createJSONEntry() {
      JSONObject json = new JSONObject();
      JSONArray decisionsJSON = new JSONArray();
      JSONArray tmpDecisionsJSON = new JSONArray();
      JSONArray conditionOrderJSON = new JSONArray();

      for (int j = 0; j < 3; j++) {
        for (int i = 0; i < decisions.get(j).size(); i++){
          tmpDecisionsJSON.put(decisions.get(j).get(i));
        }
        decisionsJSON.put(tmpDecisionsJSON);
        tmpDecisionsJSON = new JSONArray();
      }

      for (int i = 0; i < conditionOrder.size(); i++){
        conditionOrderJSON.put(conditionOrder.get(i));
      }


      json.put("decisions", decisionsJSON);
      json.put("age", this.age);
      json.put("gender", this.gender);
      json.put("name", this.name);

      return json;
    }
    public int getNextCondition() {return this.conditionOrder.get(matchCounter);}
    public void incrementMatchCounter() {this.matchCounter++;}
    public void resetScore() {this.score = 0;}
  }


}
