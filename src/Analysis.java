import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.servlet.http.Part;

public class Analysis {
  // since we automatically populate these tables we can make them a bit redundant

  // visualisation: per game, per decision a red and a green bar on top of each other
  Map<Integer, Integer[]> conditionOrderMap = Map.of(
          0, new Integer[]{0, 1,2},
          1, new Integer[]{0, 2,1},
          2, new Integer[]{1, 0,2},
          3, new Integer[]{1, 2,0},
          4, new Integer[]{2, 0,1},
          5, new Integer[]{2, 1,0}
  );

;


  public static void main(String[] args) throws Exception {
    Analysis analysis = new Analysis();
    String databaseFolder = "studyResults/";
    List<Participant> participants = new ArrayList<>();

    Iterator<File> it = FileUtils.iterateFiles(new File("./studyResults"), new String[]{
            "json"},false);

    //read all participants from the json, create participants objects and include them in a
    // large list
    while(it.hasNext()) {
      participants.addAll(analysis.loadDecisionsFromDatabase(it.next()));
      System.out.println(participants);
    }

    //delete old result statistics
    FileWriter myWriter = new FileWriter("results.txt", false);
    myWriter.close();


    //create result statistic
    analysis.resultAll(participants);
    analysis.resultOrderCondition(participants);




  }


  public void resultOrderCondition(List<Participant> participants) {
    Map<Integer, List<Integer>> ordMapOpp1 = Map.of(
            0, new ArrayList(Collections.nCopies(10, 0)),
            1, new ArrayList(Collections.nCopies(10, 0)),
            2, new ArrayList(Collections.nCopies(10, 0)),
            3, new ArrayList(Collections.nCopies(10, 0)),
            4, new ArrayList(Collections.nCopies(10, 0)),
            5, new ArrayList(Collections.nCopies(10, 0))
    );

    Map<Integer, List<Integer>> ordMapOpp2 = Map.of(
            0, new ArrayList(Collections.nCopies(9, 0)),
            1, new ArrayList(Collections.nCopies(9, 0)),
            2, new ArrayList(Collections.nCopies(9, 0)),
            3, new ArrayList(Collections.nCopies(9, 0)),
            4, new ArrayList(Collections.nCopies(9, 0)),
            5, new ArrayList(Collections.nCopies(9, 0))
    );

    Map<Integer, List<Integer>> ordMapOpp3 = Map.of(
            0, new ArrayList(Collections.nCopies(8, 0)),
            1, new ArrayList(Collections.nCopies(8, 0)),
            2, new ArrayList(Collections.nCopies(8, 0)),
            3, new ArrayList(Collections.nCopies(8, 0)),
            4, new ArrayList(Collections.nCopies(8, 0)),
            5, new ArrayList(Collections.nCopies(8, 0))
    );

    for (Participant participant : participants) {
      for (int decCount = 0; decCount < participant.getDecisionsOp1().size(); decCount++) {
        if (participant.getDecisionsOp1().get(decCount)) {
          ordMapOpp1.get(participant.getCondition()).set(decCount,
                  ordMapOpp1.get(participant.getCondition()).get(decCount) + 1);
        }
      }
      for (int decCount = 0; decCount < participant.getDecisionsOp2().size(); decCount++) {
        if (participant.getDecisionsOp2().get(decCount)) {
          ordMapOpp2.get(participant.getCondition()).set(decCount,
                  ordMapOpp2.get(participant.getCondition()).get(decCount) + 1);
        }
      }
      for (int decCount = 0; decCount < participant.getDecisionsOp3().size(); decCount++) {
        if (participant.getDecisionsOp3().get(decCount)) {
          ordMapOpp3.get(participant.getCondition()).set(decCount,
                  ordMapOpp3.get(participant.getCondition()).get(decCount) + 1);
        }
      }
    }

    for (int i = 0; i < 6; i++){
      this.createExcel("conditionOrder " + i + " vs. opponent 1", ordMapOpp1.get(i), false);
      this.createExcel("conditionOrder " + i + " vs. opponent 2", ordMapOpp2.get(i), true);
      this.createExcel("conditionOrder " + i + " vs. opponent 3", ordMapOpp3.get(i), true);
      this.createExcel("half of participants", new ArrayList(Collections.nCopies(10, 2)),
              true);

    }

  }

  public void resultAll(List<Participant> participants) {
    // count all
    List<Integer> allOp1 = new ArrayList(Collections.nCopies(10, 0));
    List<Integer> allOp2 = new ArrayList(Collections.nCopies(9, 0));
    List<Integer> allOp3 = new ArrayList(Collections.nCopies(8, 0));

    int participantID = 0;
    for (Participant participant : participants) {
      System.out.println(participantID);
      participantID++;
      for (int decCount = 0; decCount < participant.getDecisionsOp1().size(); decCount++) {
        if (participant.getDecisionsOp1().get(decCount)) {
          allOp1.set(decCount, allOp1.get(decCount) + 1);
        }
      }

      for (int decCount = 0; decCount < participant.getDecisionsOp2().size(); decCount++) {
        if (participant.getDecisionsOp2().get(decCount)) {
          allOp2.set(decCount, allOp2.get(decCount) + 1);
        }
      }

      for (int decCount = 0; decCount < participant.getDecisionsOp3().size(); decCount++) {
        if (participant.getDecisionsOp3().get(decCount)) {
          allOp3.set(decCount, allOp3.get(decCount) + 1);
        }
      }
    }

    this.createExcel("all vs. opponent 1", allOp1, false);
    this.createExcel("all vs. opponent 2", allOp2, true);
    this.createExcel("all vs. opponent 3", allOp3, true);
    this.createExcel("half of participants", new ArrayList(Collections.nCopies(10, 12)),
            true);

  }
  public class Participant {
    int age;
    String gender;
    String name;

    int condition;

    public List<Boolean> getDecisionsOp1() {
      return decisionsOp1;
    }

    public List<Boolean> getDecisionsOp2() {
      return decisionsOp2;
    }

    public List<Boolean> getDecisionsOp3() {
      return decisionsOp3;
    }

    public int getCondition(){return this.condition;}
    List<Boolean> decisionsOp1 = new ArrayList<>();
    List<Boolean> decisionsOp2 = new ArrayList<>();
    List<Boolean> decisionsOp3 = new ArrayList<>();


    public Participant() {
    }

    public void setDecision(int index, List<Boolean> list){
      switch (index) {
        case (0):
          decisionsOp1 = list;
          break;
        case (1):
          decisionsOp2 = list;
          break;
        case(2):
          decisionsOp3 = list;
          break;
        default:
          System.out.printf("Wrong opponent index: %d.\n", index);
      }
    }
    public void setCondition (int condition) {
      this.condition = condition;
    }
  }

  public List<Participant> loadDecisionsFromDatabase(File f) throws IOException {

    JSONObject database;
    JSONArray participantsJSON;
    JSONArray allDecisionsJSON;
    JSONArray gameDecisionsJSON;

    List<Participant> participants = new ArrayList<>();



    if (f.exists() && (f.length() > 2)) {
      InputStream is = new FileInputStream(f.getAbsolutePath());
      String jsonTxt = IOUtils.toString(is, "UTF-8");
      database = new JSONObject(jsonTxt);
      participantsJSON = database.getJSONArray("participants");


      //iterate over participants
      for (int partID = 0; partID < participantsJSON.length(); partID++) {
        allDecisionsJSON = participantsJSON.getJSONObject(partID).getJSONArray("decisions");

        Integer[] conditionOrder = conditionOrderMap.get(partID % 6);
        Participant participant = new Participant();

        //iterate over games
        for (int gameID = 0; gameID < 3; gameID++) {
          gameDecisionsJSON = allDecisionsJSON.getJSONArray(gameID);
          List<Boolean> tmpDecision = new ArrayList<>();

          //iterate over decisions
          for (int roundID = 0; roundID < gameDecisionsJSON.length(); roundID++) {
            tmpDecision.add(gameDecisionsJSON.getBoolean(roundID));
          }
          //we insert the decisions at this specific place to remove the conditionOrder and have
          // the decisions against the first player always in the first position and so on
          participant.setDecision(conditionOrder[gameID], tmpDecision);
          participant.setCondition(partID);
        }
        participants.add(participant);
      }

    } else {
      System.out.println("Failed to read file: " + f.getAbsolutePath());
    }

    return participants;
  }


  public void createExcel(String title, List<Integer> values, boolean append) {
    try {
      FileWriter myWriter = new FileWriter("results.txt", true);
      if(!append) {
        myWriter.write("\n");
        //start with "categories aka rounds"

        myWriter.write("\t");
        for (int i = 0; i < values.size(); i++) {
          myWriter.write("rd " + (i+1) + "\t");
        }
        myWriter.write("\n");
      }

      //now the number of times participants played fair
      myWriter.write(title + "\t");
      for (int i = 0; i < values.size(); i++) {
        myWriter.write(values.get(i) + "\t");
      }
      myWriter.write("\n");



      myWriter.close();

      System.out.println("Successfully wrote to the file.");
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}



