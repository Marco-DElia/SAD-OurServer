package RemoteCCC;


public class Config {
    private static String usr_path = System.getProperty("user.dir"); 
    final static String packageDeclaration  = "package ClientProject;\n";


    final static String pathCompiler   = usr_path + "/ClientProject/";
    final static String testingClassPath   = usr_path +"/ClientProject/src/test/java/ClientProject/";
    final static String underTestClassPath = usr_path +"/ClientProject/src/main/java/ClientProject/";
    final static String coverageFolder = usr_path + "/ClientProject/target/site/jacoco/jacoco.xml";

/* Sotto windows commentare i path stile "macOs/unix" e decommentare quelli windows
 * final static String pathCompiler   = usr_path + "\\ClientProject\\";
 * final static String testingClassPath   = usr_path +"\\ClientProject\\src\\test\\java\\ClientProject\\";
 * final static String underTestClassPath = usr_path +"\\ClientProject\\src\\main\\java\\ClientProject\\";
 * final static String coverageFolder = usr_path + "\\ClientProject\\target\\site\\jacoco\\jacoco.xml";
 */
    

    public static String getTestingClassPath ()   {return testingClassPath;   }
    public static String getUnderTestClassPath()  {return underTestClassPath; }

    public static String getpathCompiler(){return pathCompiler;}
    public static String getpackageDeclaretion(){return packageDeclaration;}
    public static String getCoverageFolder(){return coverageFolder;}

}
