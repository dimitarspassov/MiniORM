package orm.scanner;

import annotations.Entity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ClassEntityScanner {

    private final String FRAMEWORK_CLASSES_PATH="/src/main/java";
    private Map<String, Class> entities;
    private String startPath;

    public ClassEntityScanner() {
        this.entities = new HashMap<>();
    }

    public Map<String, Class> listFilesForFolder(String startPath) throws ClassNotFoundException, IOException {

        this.resetCurrentEntities();
        this.startPath=startPath;
        this.traverseFiles(startPath);
        return this.entities;
    }

    private void traverseFiles(String startPath) throws IOException, ClassNotFoundException {
        File folder=new File(startPath);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {

            if(file.getAbsolutePath().endsWith(".java")){
                String path=file
                        .getAbsolutePath()
                        .substring(this.startPath.length())
                        .replaceAll("[\\/\\\\]+",".");

                String classPath=path.substring(this.FRAMEWORK_CLASSES_PATH.length()+1);
                classPath=classPath.substring(0,classPath.length()-".java".length());

                Class tempClass=Class.forName(classPath);

                if(tempClass.isAnnotationPresent(Entity.class)){
                    this.entities.put(tempClass.getName(),tempClass);
                }
            }

            if(file.isDirectory()){
                this.traverseFiles(file.getAbsolutePath());
            }
        }
    }

    private void resetCurrentEntities() {
        this.entities = new HashMap<>();
    }
}
