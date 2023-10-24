import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import tloc.Tloc;
import tassert.Tassert;

public class App {
    public static void main(String[] args) throws Exception {
        getMetrics(args[0]);
    }

    private static ArrayList<String> getFilesInFolder(String path, boolean test) throws IOException{
        ArrayList<String> files = new ArrayList<String>();
        try (Stream<Path> stream = Files.walk(Paths.get(path))) {
            stream.filter(Files::isRegularFile)
                .forEach((file) -> {
                    //Non test classes
                    boolean condition = file.toString().endsWith(".java") && !file.toString().endsWith("Test.java");
                    if(test){
                        //test classes
                        condition = file.toString().endsWith("Test.java");
                    }
                    if(condition){
                        files.add(file.toString());
                    }
                });
        }
        return files;
    }

    private static String getFileContent(String path) throws IOException{
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String currentLine;
        String data = "";
        //Fetch all the file's content as a big string
        do{
            currentLine = reader.readLine();
            if(currentLine != null){
                currentLine = currentLine.trim();
                data += currentLine + "\n";
            }
        } while(currentLine != null);

        reader.close();
        return data;
    }

    private static void getMetrics(String folder_path) throws IOException{
        //Get test files
        ArrayList<String> test_files = getFilesInFolder(folder_path, true);
        
        //Get src files
        ArrayList<String> src_files = getFilesInFolder(folder_path, false);


        ArrayList<Map<String, String>> test_files_data = new ArrayList<Map<String, String>>();

        ArrayList<Map<String, String>> src_files_data = new ArrayList<Map<String, String>>();

        DecimalFormat df = new DecimalFormat("0.00");

        int total_test_count = 0;

        int total_loc = 0;
        int total_tloc = 0;
        int total_cloc = 0;
        int total_tassert = 0;
        int total_header_comment = 0;

        ArrayList<String> found_packages = new ArrayList<String>();
        for(int i = 0; i < test_files.size(); i++){
            
            Map<String, String> file_data = new HashMap<>();
            String content = getFileContent(test_files.get(i));

            content = removeLegalHeader(content, 35);

            String class_name = getClassName(content);

            String package_name = getPackageName(content);
            

            if(found_packages.indexOf(package_name) == -1){
                found_packages.add(package_name);
            }

            int test_count = getTestCount(content);
            total_test_count += test_count;

            int tloc = Tloc.getTloc(test_files.get(i));

            total_tloc += tloc;

            total_cloc += getCloc(content);

            int tassert = Tassert.getTassert(test_files.get(i));

            total_tassert += tassert;

            total_header_comment += getHeaderCommentCount(content);

            file_data.put("class_name", class_name);
            file_data.put("test_count", Integer.toString(test_count));
            test_files_data.add(file_data);
        }

        System.out.println(test_files_data);

        for(int i = 0; i < src_files.size(); i++){
            Map<String, String> file_data = new HashMap<>();
            String content = getFileContent(src_files.get(i));

            String class_name = getClassName(content);

            int method_count = getMethodCount(content);


            total_loc += Tloc.getTloc(src_files.get(i));

            file_data.put("class_name", class_name);
            file_data.put("method_count", Integer.toString(method_count));

            src_files_data.add(file_data);
        }
        
       
       
        double tpc = (double)total_test_count / (double)test_files_data.size();


        double tpp = (double)total_test_count / (double)found_packages.size();

        int total_method_count = 0;
        int files_not_tested = 0;
        for(int i = 0; i < test_files_data.size(); i++){
            
            Map<String, String> test_file = test_files_data.get(i);
            
            for(int j = 0; j < src_files_data.size(); j++){
                Map<String, String> src_file = src_files_data.get(j);

                String test_class_name = test_file.get("class_name");
                test_class_name = test_class_name.substring(0, test_class_name.length() - 4);

                if(test_class_name.equals(src_file.get("class_name"))){
                    
                    int test_count = Integer.parseInt(test_file.get("test_count"));
                    int method_count = Integer.parseInt(src_file.get("method_count"));

                    total_method_count += method_count;
                    if(test_count < method_count){
                        files_not_tested += method_count - test_count;
                    }

                    src_files_data.remove(j);
                    j--;
                }

            }
        }

        double pmnt = (double)files_not_tested / (double)total_method_count;

        double tloc_loc_ratio = (double)total_tloc / (double)total_loc;

        double tcmp = Double.parseDouble(df.format((double)total_tassert / (double)total_tloc));

        double dc = (double)total_cloc / (double)total_tloc;

        double header_comment_ratio = (double)total_header_comment / (double)total_test_count;

        System.out.println("TPC (tests par classe): " + df.format(tpc));
        System.out.println("TPP (tests par package): " + df.format(tpp));
        System.out.println("PMNT (pourcentage de méthodes non testés): " + df.format(pmnt * 100) + "%");
        System.out.println("Tloc / Loc (Ratio du nombre de ligne de code de test  sur le nombre de ligne de code source pour une classe): " + df.format(tloc_loc_ratio * 100) + "%");
        System.out.println("TASSERT: " + total_tassert);
        System.out.println("TCMP (Tassert / Tloc): " + df.format(tcmp * 100) + "%");
        System.out.println("DC (densité de commentaires): " + df.format(dc * 100) + "%");
        System.out.println("Ratio du nombre de fonctions contenant des commentaires en entête sur le nombre total de fonctions d'une classe de test: " + df.format(header_comment_ratio * 100) + "%");

        /*  try{
            getRepoData();
        } catch(InterruptedException e){
            System.out.println(e);
        } */
    }   

    //Get the repo's commit history
    /* private static String getRepoData(String path) throws IOException, InterruptedException{
        System.out.println(path.substring(24, path.length()));
        try {
            String apiUrl = "https://api.github.com/repos/jfree/jfreechart/commits?path=" + path.substring(24, path.length()).replace('\\', '/');
            URL url = new URL(apiUrl);

            // Set up authentication if necessary
            String authToken = "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";
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + authToken);

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder response = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse the JSON response to extract the date of the last commit
            System.out.println(response);
            //System.out.println("Last commit date of " + filePath + ": " + extractedCommitDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "HERE";
    } */

    private static int getHeaderCommentCount(String content){
        String[] lines = content.split("\n");
        String previous_line = "";
        String current_line = "";

        int comment_count = 0;

        for(int i = 0; i < lines.length; i++){
            if(!lines[i].isEmpty()){
                previous_line = current_line;
                current_line = lines[i];

                if(current_line.trim().equals("@Test") && (previous_line.trim().endsWith("*/") || previous_line.trim().startsWith("//"))){
                    comment_count++;
                }
            }
           
        }
        return comment_count;
    }

    private static String removeLegalHeader(String content, int line_count){
        String[] lines = content.split("\n");

        // Reconstruct the code by skipping the first 35 lines
        StringBuilder new_code = new StringBuilder();
        for (int i = line_count; i < lines.length; i++) {
            new_code.append(lines[i]);
            if (i < lines.length - 1) {
                new_code.append("\n"); // Add a line break if not the last line
            }
        }

        return new_code.toString();
    }

    private static int getTestCount(String content){
        content = removeCommentsFromFile(content);

        return content.split("@Test").length - 1;
    }

    private static int getMethodCount(String content){
        content = removeCommentsFromFile(content);

        // Regular expression to match method declarations
        String regex = "(?:(?:public|private|protected|static|final|native|synchronized|abstract|transient)+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(regex);

        // Create a Matcher object
        Matcher matcher = pattern.matcher(content);

        int method_count = 0;

        // Count the methods
        while (matcher.find()) {
            String method_declaration = matcher.group(0);
            method_declaration = method_declaration.split("\n")[0];
            String[] declaration_parts = method_declaration.split(" ");
            String method_name = declaration_parts[2];
            String static_method_name = "";
            if(declaration_parts.length > 3){
                static_method_name = method_declaration.split(" ")[3];
            }
           
            // Check if the method name starts with "get" or "set" and exclude it
            if (!method_name.startsWith("set") && !method_name.startsWith("get") 
                && !static_method_name.startsWith("set") && !static_method_name.startsWith("get")) {
                
                method_count++;
            }
        }
        return method_count;
    }


    private static String removeCommentsFromFile(String content){
        //Remove all the content between comments
        content = removeContentBetweenDelimiters(content, "/*", "*/");
        content = removeContentBetweenDelimiters(content, "//", "\n");

        return content;
    }

    private static int getCloc(String content){
        //Remove all the content between comments
        String data = getContentBetweenDelimiters(content);

        String[] lines = data.split("\n");

        int cloc = 0;

        //Count every line that isn't empty
        for(int i = 0; i < lines.length; i++){
            if(!lines[i].trim().isEmpty()){
                cloc++;
            }
        }

        return cloc;
    }

    public static String getContentBetweenDelimiters(String content) {
        String data = "";
        // Regular expression to match both block and single-line comments
        String regex = "(\\/\\*[^*]*\\*+(?:[^/*][^*]*\\*+)*\\/|\\/\\/[^\\r\\n]*)";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            String comment = matcher.group(1);
            data += comment;
        }
        return data;
    }

    //Function that removes all content between delimiters except "\n" including the delimiters themselves
    public static String removeContentBetweenDelimiters(String input, String startDelimiter, String endDelimiter) {
        //Pattern matches the given delimiters
        String patternString = Pattern.quote(startDelimiter) + ".*?" + Pattern.quote(endDelimiter);
        Pattern pattern = Pattern.compile(patternString, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
           // Preserve line breaks if they existed in the original input
           String replacement = matcher.group().replaceAll("[^\n]", "");
           matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }


    private static String getPackageName(String content)throws IOException{
        String package_name = "";
        String package_pattern = "package\\s+([\\w.]+);";
        Pattern package_regex = Pattern.compile(package_pattern);
        Matcher package_matcher = package_regex.matcher(content);
        if (package_matcher.find()) {
            package_name = package_matcher.group(1);
        } 
        return package_name;
    }

    private static String getClassName(String content)throws IOException{
        content = removeCommentsFromFile(content);
        String class_name = "";
        Pattern pattern = Pattern.compile("\\b(public|private|protected|static|final)\\s+class\\s+(\\w+)");

        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            class_name = matcher.group(2);
        }

        

        return class_name;
    }

}
