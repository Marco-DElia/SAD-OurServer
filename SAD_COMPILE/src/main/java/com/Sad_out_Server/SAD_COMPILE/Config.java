package com.Sad_out_Server.SAD_COMPILE;



public class Config {
    final static String path_for_compiler = "/Users/emanuele/Desktop/SAD_COMPILE/testfile/maven-code-coverage";
    final static String packageDeclaration = "package com.mkyong.examples;\n";
    final static String zipSiteFolderToJSON = "/Users/emanuele/Desktop/SAD_COMPILE/testfile/maven-code-coverage/target/";
    final static String path_for_save = "/Users/emanuele/Desktop/SAD_COMPILE/testfile/maven-code-coverage/src/";
    final static String second_path_for_save ="/java/com/mkyong/examples/";
    static String get_secondpath_for_save(){return second_path_for_save;}
    static String get_pathforsave(){return path_for_save;}
    static String get_pathforcompiler(){return path_for_compiler;}
    static String get_packageDeclaretion(){return packageDeclaration;}
    static String getzipSiteFolderJSON(){return zipSiteFolderToJSON;}

}
