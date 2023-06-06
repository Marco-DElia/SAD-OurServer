package RemoteCCC;


public class Config {
    private static String usr_path = System.getProperty("user.dir"); 
    final static String pathCompiler   = usr_path + "/ClientProject/";
    final static String packageDeclaration  = "package ClientProject;\n";
    final static String zipSiteFolderToJSON = usr_path +"/ClientProject/target/";
    final static String testingClassPath   = usr_path +"/ClientProject/src/test/java/ClientProject/";
    final static String underTestClassPath = usr_path +"/ClientProject/src/main/java/ClientProject/";
    

    public static String getTestingClassPath ()   {return testingClassPath;   }
    public static String getUnderTestClassPath()  {return underTestClassPath; }

    public static String getpathCompiler(){return pathCompiler;}
    public static String getpackageDeclaretion(){return packageDeclaration;}
    public static String getzipSiteFolderJSON(){return zipSiteFolderToJSON;}

}
