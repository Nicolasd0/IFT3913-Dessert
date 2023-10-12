import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

        for(int i = 0; i < test_files.size(); i++){
            Map<String, String> file_data = new HashMap<>();
            String content = getFileContent(test_files.get(i));

            String package_name = getPackageName(content);
            String class_name = getClassName(content);

            int test_count = getTestCount(content);

            file_data.put("package_name", package_name);
            file_data.put("class_name", class_name);
            file_data.put("test_count", Integer.toString(test_count));

            test_files_data.add(file_data);
        }



        for(int i = 0; i < src_files.size(); i++){
            Map<String, String> file_data = new HashMap<>();
            String content = getFileContent(src_files.get(i));

            String class_name = getClassName(content);

            int method_count = getMethodCount(content);

            file_data.put("class_name", class_name);
            file_data.put("method_count", Integer.toString(method_count));
            file_data.put("file_path", src_files.get(i));

            src_files_data.add(file_data);
        }
        
        System.out.println(src_files_data);
        
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
            method_count++;
        }
        return method_count;
    }


    private static String removeCommentsFromFile(String content){
        //Remove all the content between comments
        content = removeContentBetweenDelimiters(content, "/*", "*/");
        content = removeContentBetweenDelimiters(content, "//", "\n");

        return content;
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
