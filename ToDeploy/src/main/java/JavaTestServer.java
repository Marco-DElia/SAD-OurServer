import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@SpringBootApplication
@RestController
public class JavaTestServer {

    public static void main(String[] args) {
        SpringApplication.run(JavaTestServer.class, args);
        System.out.println("Server started on port 8080");
    }

    @PostMapping("/runTest")
    public ResponseEntity<String> runTest(@RequestBody TestRequest request) throws IOException {
        String classCode = null;
        String testCode = null;

        // Estrae il codice delle classi dal contenuto dei file
        for (MultipartFile file : request.getFiles()) {
            if (file.getName().equals("classFile")) {
                classCode = new String(file.getBytes());
            } else if (file.getName().equals("testFile")) {
                testCode = new String(file.getBytes());
            }
        }

        if (classCode != null && testCode != null) {
            // Scrive il codice delle classi su file temporanei
            String className = "Main_" + UUID.randomUUID().toString().replaceAll("-", "");
            File classFile = new File(className + ".java");
            Files.writeString(classFile.toPath(), classCode);
            File testFile = new File(className + "Test.java");
            Files.writeString(testFile.toPath(), testCode);

            // Compila il codice delle classi
            Process compileProcess = Runtime.getRuntime().exec("javac " + classFile.getAbsolutePath() + " " + testFile.getAbsolutePath());
            try {
                compileProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (compileProcess.exitValue() != 0) {
                // Se la compilazione fallisce, restituisce un errore
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore nella compilazione del codice");
            }

            // Esegue i test
            Process runProcess = Runtime.getRuntime().exec("java " + className + "Test");
            try {
                runProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (runProcess.exitValue() == 0) {
                // Se i test passano, restituisce un messaggio di successo
                return ResponseEntity.ok("Test passati con successo!");
            } else {
                // Se i test falliscono, restituisce un errore
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Uno o più test falliti");
            }
        } else {
            // Se la richiesta è incompleta, restituisce un errore
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Richiesta incompleta");
        }
    }

    public static class TestRequest {
        private MultipartFile[] files;

        public MultipartFile[] getFiles() {
            return files;
        }

        public void setFiles(MultipartFile[] files) {
            this.files = files;
        }
    }
}