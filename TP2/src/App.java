import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
        //Main metrics function call
        String metrics = getMetrics(args[0]);

        //Optionally print to CSV file
        if(args.length > 1){
            String header = "TPC, TPP, PMNT, Tloc / Loc, Tassert, Tcmp, DC, Header Comment Ratio";
            writeToCSV(args[1], header, metrics);
        }
       
    }

    //Gets all file paths from the root folder of a project 
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

    //Function to print to a CSV
    private static void writeToCSV(String path, String header, String data) throws IOException{
        FileWriter fileWriter = new FileWriter(path);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(header);
        bufferedWriter.newLine();
        bufferedWriter.write(data);
        bufferedWriter.newLine();
        bufferedWriter.close();
    }

    private static String getMetrics(String folder_path) throws IOException{
        //Get test files
        ArrayList<String> test_files = getFilesInFolder(folder_path, true);
        
        //Get src files
        ArrayList<String> src_files = getFilesInFolder(folder_path, false);


        ArrayList<Map<String, String>> test_files_data = new ArrayList<Map<String, String>>();

        ArrayList<Map<String, String>> src_files_data = new ArrayList<Map<String, String>>();

        DecimalFormat df = new DecimalFormat("0.00");

        //Counts that will be used to calculate the different metrics
        int total_test_count = 0;
        int total_loc = 0;
        int total_tloc = 0;
        int total_cloc = 0;
        int total_tassert = 0;
        int total_header_comment = 0;

        //Unique package list
        ArrayList<String> found_packages = new ArrayList<String>();

        //Metrics related to test files
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

        //Metrics related to source files
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


        //Metrics that require mapping between test class and source class
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

        //Aggregation of metrics
        double tpc = (double)total_test_count / (double)test_files_data.size();
        double tpp = (double)total_test_count / (double)found_packages.size();

        double pmnt = (double)files_not_tested / (double)total_method_count;

        double tloc_loc_ratio = (double)total_tloc / (double)total_loc;

        double tcmp = Double.parseDouble(df.format((double)total_tassert / (double)total_tloc));

        double dc = (double)total_cloc / (double)total_tloc;

        double header_comment_ratio = (double)total_header_comment / (double)total_test_count;

        //Print metrics to console
        System.out.println("TPC, TPP, PMNT, Tloc / Loc, Tassert, Tcmp, DC, Header Comment Ratio");

        System.out.println(String.format("%s, %s, %s, %s, %s, %s, %s, %s", df.format(tpc), df.format(tpp), df.format(pmnt * 100) + "%", df.format(tloc_loc_ratio * 100) + "%", total_tassert, df.format(tcmp * 100) + "%", df.format(dc * 100) + "%", df.format(header_comment_ratio * 100) + "%"));

        return String.format("%s, %s, %s, %s, %s, %s, %s, %s", df.format(tpc), df.format(tpp), df.format(pmnt * 100) + "%", df.format(tloc_loc_ratio * 100) + "%", total_tassert, df.format(tcmp * 100) + "%", df.format(dc * 100) + "%", df.format(header_comment_ratio * 100) + "%");
    }   

    //Calculates the amount of methods that contain a comment before it's declaration
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

    //Each file has a commented legal header, we remove it for our metrics to be more accurate
    private static String removeLegalHeader(String content, int line_count){
        String[] lines = content.split("\n");

        //Reconstruct the code by skipping the first 35 lines
        StringBuilder new_code = new StringBuilder();
        for (int i = line_count; i < lines.length; i++) {
            new_code.append(lines[i]);
            if (i < lines.length - 1) {
                new_code.append("\n"); // Add a line break if not the last line
            }
        }

        return new_code.toString();
    }

    //Get's the amount of tests in a class
    private static int getTestCount(String content){
        content = removeCommentsFromFile(content);

        return content.split("@Test").length - 1;
    }

    //Get's the amount of methods in a class
    private static int getMethodCount(String content){
        content = removeCommentsFromFile(content);

        //Regular expression to match method declarations
        String regex = "(?:(?:public|private|protected|static|final|native|synchronized|abstract|transient)+\\s+)+[$_\\w<>\\[\\]\\s]*\\s+[\\$_\\w]+\\([^\\)]*\\)?\\s*\\{?[^\\}]*\\}?";

        //Create a Pattern object
        Pattern pattern = Pattern.compile(regex);

        //Create a Matcher object
        Matcher matcher = pattern.matcher(content);

        int method_count = 0;

        //Count the methods
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

    //Remove's comments from file content
    private static String removeCommentsFromFile(String content){
        //Remove all the content between comments
        content = removeContentBetweenDelimiters(content, "/*", "*/");
        content = removeContentBetweenDelimiters(content, "//", "\n");

        return content;
    }

    //Get the amount of commented lines in a file
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

    //Util function used by getCloc
    public static String getContentBetweenDelimiters(String content) {
        String data = "";
        //Regular expression to match both block and single-line comments
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

    //Identifies the package name for a given file
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

    //Identifies the class name for a given file
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
