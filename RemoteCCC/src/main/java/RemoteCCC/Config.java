package RemoteCCC;

//ClientProject\src\main\java\ClientProject

public class Config {
    final static String path_for_compiler   = "ClientProject/";
    final static String packageDeclaration  = "package ClientProject;\n";
    final static String zipSiteFolderToJSON = "ClientProject/target/";
    final static String testingClassPath   = "ClientProject/src/test/java/ClientProject/";
    final static String underTestClassPath = "ClientProject/src/main/java/ClientProject/";
    

    public static String getTestingClassPath ()   {return testingClassPath;   }
    public static String getUnderTestClassPath()  {return underTestClassPath; }

    public static String get_pathforcompiler(){return path_for_compiler;}
    public static String get_packageDeclaretion(){return packageDeclaration;}
    public static String getzipSiteFolderJSON(){return zipSiteFolderToJSON;}

}
