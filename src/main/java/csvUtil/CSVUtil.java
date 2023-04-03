package csvUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CSVUtil {
    /**
     * Reads info from file into a list of list of strings
     * @param fileName string
     * @return list of list of strings
     */
    public List<List<String>> importData(String fileName) {
        List<List<String>> data = new ArrayList<>();
        Path path = Paths.get(fileName);

        // Create a stream to read the CSV file
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            data = parseCSVString(reader);
        }
        catch (IOException e){
            System.out.println("Error: can't open file " + fileName);
        }
        return data;
    }

    /**
     * Reads info from reader into a list of list of strings
     * Needed for API Dao
     * @param reader Buffered Reader
     */
    public List<List<String>> parseCSVString(BufferedReader reader) throws IOException {
        List<List<String>> data = new ArrayList<>();
        String line;

        //skip header
        reader.readLine();

        try {
            while ((line = reader.readLine()) != null) {
                //split line at commas except when the comma is within double quotes
                //reference: https://stackoverflow.com/questions/18893390/splitting-on-comma-outside-quotes
                List<String> info = List.of(line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"));
                data.add(info);
            }
        } catch (IOException e){
            System.out.println("Error parseing CSV string");
        }
        return data;
    }

    /**
     * change string into int or 0 if string is empty
     * @param str - String
     * @return int
     */
    public static int intFromString(String str){
        if (str.equals("")){
            return 0;
        }
        return Integer.parseInt(str.replaceAll(" ", ""));
    }
}
