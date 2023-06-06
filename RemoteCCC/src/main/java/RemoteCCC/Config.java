package RemoteCCC;


public class Config {
    final static String pathCompiler   = "ClientProject/";
    final static String packageDeclaration  = "package ClientProject;\n";
    final static String zipSiteFolderToJSON = "ClientProject/target/";
    final static String testingClassPath   = "ClientProject/src/test/java/ClientProject/";
    final static String underTestClassPath = "ClientProject/src/main/java/ClientProject/";
    

    public static String getTestingClassPath ()   {return testingClassPath;   }
    public static String getUnderTestClassPath()  {return underTestClassPath; }

    public static String getpathCompiler(){return pathCompiler;}
    public static String getpackageDeclaretion(){return packageDeclaration;}
    public static String getzipSiteFolderJSON(){return zipSiteFolderToJSON;}

}
