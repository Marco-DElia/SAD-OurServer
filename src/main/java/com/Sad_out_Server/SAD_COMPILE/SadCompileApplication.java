package com.Sad_out_Server.SAD_COMPILE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;


import java.util.ArrayList;
import java.util.List;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;



@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ServletComponentScan
@RestController
public class SadCompileApplication {

    private static final String COMPILER_OUTPUT_DIR = "compiled";
    public static void main(String[] args) {
		SpringApplication.run(SadCompileApplication.class, args);
	}


       /**
     * REST endpoint for handling POST requests with JSON body containing two Java files.
     * Compiles the two files, runs the test file on the compiled first file, and measures test coverage with Jacoco.
     * @param request The JSON request containing the two Java files.
     * @return A JSON response containing the test coverage results.
     * @throws IOException If there is an I/O error reading or writing files.
     * @throws InterruptedException
     */
    @PostMapping(value = "/compile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String compileAndTest(@RequestBody RequestDTO request) throws IOException, InterruptedException {
        String firstCode = request.getFirstCode();
        String secondCode = request.getSecondCode();

        // Salvataggio dei due file su disco: occorre specificare il nome della classe, per la corretta compilazione
        Path firstFilePath = saveCodeToFile("ClassUnderTest.java",firstCode, "main");
        saveCodeToFile("TestClassUnderTest.java",secondCode, "test");

        // Compile the two files.
    
        List<String> compilationErrors = compileSingleFile(Arrays.asList(firstFilePath.toString()));
        if (!compilationErrors.isEmpty()) {
            return "{\"error\": \"Compilation failed. Errors: " + String.join(", ", compilationErrors) + "\"}";
        }
        
        // Run the test file and measure coverage with Jacoco.
        // Return the coverage result as a JSON response.
        
    
        executeMavenCleanTestWithPath();


        String path = "/Users/emanuele/Desktop/SAD_COMPILE/testfile/maven-code-coverage/target/site/jacoco";
        String filePath = Paths.get(path, "index.html").toString();
        byte[] encodedBytes = Files.readAllBytes(Paths.get(filePath));
        return "{\"result:\"" + new String(encodedBytes)+"}";
    }


    public static List<String> compileSingleFile(List<String> list) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.addAll(list);
        args.add("-d");
        args.add(COMPILER_OUTPUT_DIR);
    
        OutputStream out_error = new ByteArrayOutputStream();
    
        int result = compiler.run(null, null, out_error, args.toArray(new String[0]));
    
        if (result == 0) {
            return new ArrayList<>();
        } else {
            String[] errors = out_error.toString().split(System.lineSeparator());
            return Arrays.asList(errors);
        }
    }
        


    public static void executeMavenCleanTestWithPath() throws IOException, InterruptedException {
        String workingDir = "/Users/emanuele/Desktop/SAD_COMPILE/testfile/maven-code-coverage";
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("mvn", "clean", "test");
        processBuilder.directory(new File(workingDir));
    
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
    
        if (exitCode == 0) {
            System.out.println("Maven clean test executed successfully.");
        } else {
            System.out.println("Error executing Maven clean test.");
        }
    }
   

    private Path saveCodeToFile(String nameclass, String code, String path) throws IOException {

        File tempFile = new File("/Users/emanuele/Desktop/SAD_COMPILE/testfile/maven-code-coverage/src/"+path+"/java/com/mkyong/examples/" + nameclass); 
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }
        return tempFile.toPath();
    }
   

    private static class RequestDTO {
        private String firstCode;
        private String secondCode;

        public String getFirstCode() {
            return firstCode;
        }

        public String getSecondCode() {
            return secondCode;
        }
    }


}
