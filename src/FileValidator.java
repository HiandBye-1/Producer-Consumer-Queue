package src;
import java.io.File;
import java.util.Scanner;
import java.util.HashMap;


public class FileValidator {
    public static void main(String[] args) throws Exception {

    }
    public static HashMap<String, Integer> FileInput(String filename)throws Exception {
        
        HashMap<String, Integer> map = new HashMap<>();
        int line = 0;
        String input = "";

        try(Scanner in = new Scanner(new File(filename))){
            boolean ended = false;
            while(in.hasNextLine()){//stop when all inputs are read
                while(in.hasNextLine()){//check for blank and comments
                    input = in.nextLine();
                    line++;
                    if(!(input.trim().isEmpty() || input.trim().charAt(0) == '#')){
                        break;
                    }

                    if(!in.hasNextLine()){
                        ended = true;
                    }
                }//end the check  

            
                if(ended){
                    break;
                }

                String[] current = input.trim().split("=", -1);
                for(int i = 0; i < current.length; i++){
                    current[i] = current[i].trim();
                }

                if(current.length != 2){
                    throw new IllegalArgumentException("Invalid input at line " + line);
                }
                
                if(current[0].equals("seed")      ||
                current[0].equals("jobs")      ||
                current[0].equals("producers") ||
                current[0].equals("consumers") ||
                current[0].equals("capacity")  ||
                current[0].equals("delay_ms")) {
                if(map.containsKey(current[0])){
                    throw new IllegalArgumentException("Duplicate input at line " + line);
                }
                }else throw new IllegalArgumentException("Invalid input at line " + line);


                if(!isInteger(current[1])){//not integer
                    throw new IllegalArgumentException("Not an Integer at line " + line);
                }

                switch(current[0]){
                    case "seed":
                        checkRange(Integer.parseInt(current[1]), 0, 9999);
                        map.put(current[0], Integer.parseInt(current[1]));
                        break;
                    case "jobs":
                        checkRange(Integer.parseInt(current[1]), 0, 10000);
                        map.put(current[0], Integer.parseInt(current[1]));
                        break;

                    case "producers":
                        checkRange(Integer.parseInt(current[1]), 1, 8);
                        map.put(current[0], Integer.parseInt(current[1]));
                        break;

                    case "consumers":
                        checkRange(Integer.parseInt(current[1]), 1, 8);
                        map.put(current[0], Integer.parseInt(current[1]));
                        break;

                    case "capacity":
                        checkRange(Integer.parseInt(current[1]), 1, 64);
                        map.put(current[0], Integer.parseInt(current[1]));
                        break;

                    case "delay_ms":
                        checkRange(Integer.parseInt(current[1]), 0, 10);
                        map.put(current[0], Integer.parseInt(current[1]));
                        break;
                }

            }
        }


        if(map.size() != 6) throw new IllegalArgumentException("Missing one of the required 6 inputs only " + map.size() + " inputs found");

        return map;
    }


    public static boolean isInteger(String s){
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkRange(int value, int min, int max){
        if(value >= min && value <= max){
            return true;
        }else{
            throw new IllegalArgumentException("Value must be between " + min + " and " + max);
        }
    }

}
