package com.Sad_out_Server.SAD_COMPILE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.ByteArrayOutputStream;



@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ServletComponentScan
@RestController
public class SadCompileApplication {

    private static final String COMPILER_OUTPUT_DIR = "compiled";
    private static final String TEST_RUNNER_CLASS_NAME = "TestRunner";
    private static final String JACOCO_EXEC_FILE = "jacoco.exec";

	public static void main(String[] args) {
		SpringApplication.run(SadCompileApplication.class, args);
	}


       /**
     * REST endpoint for handling POST requests with JSON body containing two Java files.
     * Compiles the two files, runs the test file on the compiled first file, and measures test coverage with Jacoco.
     * @param request The JSON request containing the two Java files.
     * @return A JSON response containing the test coverage results.
     * @throws IOException If there is an I/O error reading or writing files.
     */
    @PostMapping(value = "/compile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String compileAndTest(@RequestBody RequestDTO request) throws IOException {
        String firstCode = request.getFirstCode();
        String secondCode = request.getSecondCode();

        // Salvataggio dei due file su disco: occorre specificare il nome della classe, per la corretta compilazione
        Path firstFilePath = saveCodeToFile("ClassUnderTest.java",firstCode);
        Path secondFilePath = saveCodeToFile("Test.java",secondCode);

        // Compile the two files.
    
        List<String> compilationErrors = compileFiles(Arrays.asList(firstFilePath.toString(), secondFilePath.toString()));
        if (!compilationErrors.isEmpty()) {
            return "{\"error\": \"Compilation failed. Errors: " + String.join(", ", compilationErrors) + "\"}";
        }
        
        // Run the test file and measure coverage with Jacoco.
        // Return the coverage result as a JSON response.
      
        return "{\"stato:OK\"}";
    }

    private List<String> compileFiles(List<String> fileNames) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.addAll(fileNames);
        args.add("-d");
        args.add(COMPILER_OUTPUT_DIR);
        
        OutputStream out_error = new ByteArrayOutputStream();
        
        int result = compiler.run(null, null, out_error, args.toArray(new String[0]));
        
        if (result == 0) {
            return new ArrayList<>();
        } else {
            return Arrays.asList("Compilation failed with error code " + out_error.toString());
        }
    }

    private Path saveCodeToFile(String nameclass, String code) throws IOException {
        
        File tempFile = new File("/Users/emanuele/Desktop/SAD_COMPILE/" + nameclass); 
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

        public void setFirstCode(String firstCode) {
            this.firstCode = firstCode;
        }

        public String getSecondCode() {
            return secondCode;
        }

        public void setSecondCode(String secondCode) {
            this.secondCode = secondCode;
        }
    }


}
