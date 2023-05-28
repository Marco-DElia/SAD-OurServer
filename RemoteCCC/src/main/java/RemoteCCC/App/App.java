package RemoteCCC.App;


import RemoteCCC.Config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.File;


import java.io.FileWriter;
import java.io.ByteArrayOutputStream;



@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ServletComponentScan
@RestController
public class App {

    //public static final String PROJECT_DIR = "/Users/emanuele/Desktop/SAD_COMPILE/testfile/maven-code-coverage/";

    public static void main(String[] args) {
		SpringApplication.run(App.class, args);
	}


       /**
     * REST endpoint for handling POST requests with JSON body containing two Java files.
     * Compiles the two files, runs the test file on the compiled first file, and measures test coverage with Jacoco.
     * @param request The JSON request containing the two Java files.
     * @return A JSON response containing the test coverage results.
     * @throws IOException If there is an I/O error reading or writing files.
     * @throws InterruptedException
     */
    @PostMapping(value = "/compile-and-codecoverage", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String compileAndTest(@RequestBody RequestDTO request) throws IOException, InterruptedException {
        
        String testingClassName   = request.getTestingClassName();
        String testingClassCode   = request.getTestingClassCode();

        String underTestClassName = request.getUnderTestClassName();
        String underTestClassCode = request.getUnderTestClassCode();
        

        // Salvataggio dei due file su disco: occorre specificare il nome della classe, per la corretta compilazione
        Path firstFilePath  = saveCodeToFile(testingClassName, testingClassCode, Config.getTestingClassPath());
        Path secondFilePath = saveCodeToFile(underTestClassName, underTestClassCode, Config.getUnderTestClassPath());

        
        // Compile the two files.
        addPackageDeclaration(firstFilePath, secondFilePath);

        String []output_maven={""};

        if(compileExecuteCovarageWithMaven(output_maven)){
            String zip_ret = zipSiteFolderToJSON(Config.getzipSiteFolderJSON()).toString();
            String ret =  "{\"outCompile:\":" + output_maven[0] + "\"zip:\""+zip_ret+"}";
            return ret;
        }else
        {
            String ret =  "{\"out_compile:\":" + output_maven[0] + "\"zip\":\"NO_ZIP\"}";
            return ret;
        }
    
    
    }

    public String zipSiteFolderToJSON(String path) throws IOException {
        String folderPath = Paths.get(path, "site").toString();
    
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
    
        Path folder = Paths.get(folderPath);
        Files.walk(folder)
                .filter(p -> !Files.isDirectory(p))
                .forEach(p -> {
                    try {
                        Path relativePath = folder.relativize(p);
                        ZipEntry zipEntry = new ZipEntry(relativePath.toString());
                        zos.putNextEntry(zipEntry);
                        zos.write(Files.readAllBytes(p));
                        zos.closeEntry();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    
        zos.close();
    
        byte[] zipBytes = baos.toByteArray();
        String encodedZip = Base64.getEncoder().encodeToString(zipBytes);
    
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("filename", "site.zip");
        jsonObject.put("zip", encodedZip);
    
        return jsonObject.toString();
    }

    //FUNZIONE DI UTILITà SERVE PER AGGIUNGERE IL PACKAGE alla dichiarazione
    public static void addPackageDeclaration(Path file1Path, Path file2Path) throws IOException {
        String packageDeclaration = Config.get_packageDeclaretion();
    
        String file1Content = Files.readString(file1Path, StandardCharsets.UTF_8);
        String file2Content = Files.readString(file2Path, StandardCharsets.UTF_8);
    
        file1Content = packageDeclaration + file1Content;
        file2Content = packageDeclaration + file2Content;
    
        Files.write(file1Path, file1Content.getBytes(StandardCharsets.UTF_8));
        Files.write(file2Path, file2Content.getBytes(StandardCharsets.UTF_8));
    }


    //esegue compilazione con maven per ritornare eventuali errori utente
    public static boolean compileExecuteCovarageWithMaven(String []ret) throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder();

        processBuilder.command("mvn", "clean", "compile", "test");
        processBuilder.directory(new File(Config.get_pathforcompiler()));
    
        Process process = processBuilder.start();
        int exitCode = process.waitFor();
       
        // Legge il contenuto del buffer del terminale
        InputStream inputStream = process.getInputStream();
        byte[] buffer = new byte[inputStream.available()];
        inputStream.read(buffer);
        ret[0] = new String(buffer, StandardCharsets.UTF_8);
        if (exitCode == 0) {
            System.out.println("Maven clean compile executed successfully.");
            return true;
        } else {
            System.out.println("Error executing Maven clean compile.");
            return false;
        }

    }
   

    private Path saveCodeToFile(String nameclass, String code, String path) throws IOException {

        File tempFile = new File(path + nameclass);
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }
        return tempFile.toPath();
    }
   

    private static class RequestDTO {

        private String testingClassName;
        private String testingClassCode;
        
        private String underTestClassName;
        private String underTestClassCode;


        public String getTestingClassName(){
            return testingClassName;
        }
        
        public String getTestingClassCode() {
            return testingClassCode;
        }


        public String getUnderTestClassName(){
            return underTestClassName;
        }

        public String getUnderTestClassCode() {
            return underTestClassCode;
        }     
        
    }


}
