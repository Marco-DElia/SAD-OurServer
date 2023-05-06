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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ServletComponentScan
@RestController
public class ServerApplication {

    private static final String COMPILER_OUTPUT_DIR = "compiled";
    private static final String TEST_RUNNER_CLASS_NAME = "TestRunner";
    private static final String JACOCO_EXEC_FILE = "jacoco.exec";

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    /**
     * REST endpoint for handling POST requests with JSON body containing two Java files.
     * Compiles the two files, runs the test file on the compiled first file, and measures test coverage with Jacoco.
     * @param request The JSON request containing the two Java files.
     * @return A JSON response containing the test coverage results.
     * @throws IOException If there is an I/O error reading or writing files.
     */
    @PostMapping(value = "/compile-and-test", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String compileAndTest(@RequestBody RequestDTO request) throws IOException {
        String firstCode = request.getFirstCode();
        String secondCode = request.getSecondCode();

        // Save the two files to disk.
        Path firstFilePath = saveCodeToFile(firstCode);
        Path secondFilePath = saveCodeToFile(secondCode);

        // Compile the two files.
        List<String> compilationErrors = compileFiles(Arrays.asList(firstFilePath.toString(), secondFilePath.toString()));
        if (!compilationErrors.isEmpty()) {
            return "{\"error\": \"Compilation failed. Errors: " + String.join(", ", compilationErrors) + "\"}";
        }

        // Run the test file and measure coverage with Jacoco.
        int coveragePercentage = runTestAndGetCoverage(firstFilePath, secondFilePath);

        // Return the coverage result as a JSON response.
        return "{\"coverage\": " + coveragePercentage + "}";
    }

    /**
     * Saves the given code to a temporary file and returns the path to the file.
     * @param code The code to save to a file.
     * @return The path to the saved file.
     * @throws IOException If there is an I/O error writing the file.
     */
    private Path saveCodeToFile(String code) throws IOException {
        File tempFile = File.createTempFile("code", ".java");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }
        return tempFile.toPath();
    }

    /**
     * Compiles the given list of Java files using the JavaCompiler API.
     * @param fileNames The list of file names to compile.
     * @return A list of compilation errors, or an empty list if there were no errors.
     */
    private List<String> compileFiles(List<String> fileNames) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        List<String> args = new ArrayList<>();
        args.addAll(fileNames);
        args.add("-d");
        args.add(COMPILER_OUTPUT_DIR);
        int result = compiler.run(null, null, null, args.toArray(new String[0]));
        if (result == 0) {
            return new ArrayList<>();
        } else {
            return Arrays.asList("Compilation failed with error code " + result);
        }
    }

    /**
     * Runs the test file on the compiled first file and measures test coverage with Jacoco.
     * @param firstFilePath The path to the compiled first file.
     * @param secondFilePath The path to the test file.
     * @return The test coverage percentage.
     * @throws IOException If there is an I/O error running the test or measuring coverage.
     */
    private int runTestAndGetCoverage(Path firstFilePath, Path secondFilePath) throws IOException {
        // Create a class loader that loads the compiled first file and the test file.
        ClassLoader loader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(TEST_RUNNER_CLASS_NAME)) {
                    try {
                        byte[] testBytes = Files.readAllBytes(secondFilePath);
                        return defineClass(TEST_RUNNER_CLASS_NAME, testBytes, 0, testBytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Could not load test file", e);
                    }
                } else {
                    try {
                        byte[] classBytes = Files.readAllBytes(firstFilePath.getParent().resolve(name.replace('.', '/') + ".class"));
                        return defineClass(name, classBytes, 0, classBytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Could not load class file", e);
                    }
                }
            }
        };

        // Run the test file using JUnit.
        org.junit.runner.JUnitCore junit = new org.junit.runner.JUnitCore();
        junit.addListener(new org.junit.internal.TextListener(System.out));
        Class<?> testClass;
        try {
            testClass = loader.loadClass(TEST_RUNNER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new IOException("Could not load test class", e);
        }
        junit.run(testClass);

        // Measure test coverage with Jacoco.
        Path execFilePath = Paths.get(JACOCO_EXEC_FILE);
        if (Files.exists(execFilePath)) {
            Files.delete(execFilePath);
        }
        org.jacoco.agent.rt.internal_8ff85ea.core.runtime.Agent.getInstance().reset();
        org.jacoco.agent.rt.internal_8ff85ea.core.runtime.Agent.getInstance().start(null);
        junit.run(testClass);
        org.jacoco.agent.rt.internal_8ff85ea.core.runtime.Agent.getInstance().stop();
        Files.write(execFilePath, org.jacoco.agent.rt.internal_8ff85ea.core.data.ExecutionDataWriter.toByteArray());

        org.jacoco.cli.internal.commands.Report report = new org.jacoco.cli.internal.commands.Report();
        report.setSessionId("compile-and-test");
        report.setExecutionDataFile(execFilePath.toString());
        report.setClassDirectories(COMPILER_OUTPUT_DIR);
        report.setOutputDirectory("jacoco-report");
        report.execute();

        org.jacoco.report.IReportVisitor visitor = new org.jacoco.report.html.HTMLFormatter().createVisitor(new java.io.PrintWriter(System.out));
        org.jacoco.report.IReportGroupVisitor groupVisitor = visitor.visitGroup("Compile and Test Coverage");
        groupVisitor.visitBundle(new org.jacoco.report.DirectorySourceFileLocator(new File(COMPILER_OUTPUT_DIR), "UTF-8"), new org.jacoco.report.DirectoryClassLocator(new File(COMPILER_OUTPUT_DIR)), "");
        groupVisitor.visitEnd();
        visitor.visitEnd();

        org.jacoco.report.IReportVisitor visitor2 = new org.jacoco.report.xml.XMLFormatter().createVisitor(new java.io.PrintWriter(System.out));
        org.jacoco.report.IReportGroupVisitor groupVisitor2 = visitor2.visitGroup("Compile and Test Coverage");
        groupVisitor2.visitBundle(new org.jacoco.report.DirectorySourceFileLocator(new File(COMPILER_OUTPUT_DIR), "UTF-8"), new org.jacoco.report.DirectoryClassLocator(new File(COMPILER_OUTPUT_DIR)), "");
        groupVisitor2.visitEnd();
        visitor2.visitEnd();

        String reportXml = new String(Files.readAllBytes(Paths.get("jacoco-report/coverage.xml")));
        int coveragePercentage = Integer.parseInt(reportXml.replaceAll(".*<counter type=\"INSTRUCTION\" missed=\"(\\d+)\" covered=\"(\\d+)\"\\s*/>.*", "$2")) * 100 / Integer.parseInt(reportXml.replaceAll(".*<counter type=\"INSTRUCTION\" missed=\"(\\d+)\" covered=\"(\\d+)\"\\s*/>.*", "$1"));
        return coveragePercentage;
    }

    /**
     * A DTO class representing the JSON request body.
     */
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

    /**
     * A DTO class representing the JSON response body.
     */
    private static class ResponseDTO {
        private int coverage;

        public int getCoverage() {
            return coverage;
        }

        public void setCoverage(int coverage) {
            this.coverage = coverage;
        }
    }
}