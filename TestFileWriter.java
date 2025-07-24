package com.yourplugin.sparklineageplugin;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.psi.impl.file.PsiJavaDirectoryImpl;
import com.intellij.ide.highlighter.JavaFileType;
import java.io.File;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.command.WriteCommandAction;
public class TestFileWriter {

   /* public static String writeTestFile(Project project, PsiFile baseFile, String packageName, String testClassName, String content) {
        PsiDirectory directory = baseFile.getContainingDirectory();
        PsiFileFactory factory = PsiFileFactory.getInstance(project);
        PsiFile testFile = factory.createFileFromText(testClassName + ".java", JavaFileType.INSTANCE, content);
        directory.add(testFile);
        return directory.getVirtualFile().getPath() + "/" + testClassName + ".java";
    }*/


   /* public static String writeTestFile(Project project, PsiFile baseFile, String packageName, String testClassName, String content) {
        // Step 1: Get the base file path and convert to test path
        String basePath = baseFile.getVirtualFile().getPath(); // e.g. /.../src/main/java/com/xyz/Foo.java

        System.out.println("basePath : "+basePath);
        String testPath = basePath.replace("src/main/java", "src/test/java");

        // Step 2: Trim filename from path to get directory path
        String testDirPath = testPath.substring(0, testPath.lastIndexOf(File.separator));

        // Step 3: Find or create test directory
        VirtualFile testDirVF = LocalFileSystem.getInstance().findFileByIoFile(new File(testDirPath));
        if (testDirVF == null) {
            try {
                VirtualFile srcTestJavaVF = LocalFileSystem.getInstance()
                        .findFileByIoFile(new File(project.getBasePath() + "/src/test/java"));

                if (srcTestJavaVF == null) {
                    System.err.println("src/test/java folder not found.");
                    return null;
                }

                String[] packageSegments = packageName.split("\\.");
                testDirVF = createDirectories(srcTestJavaVF, packageSegments);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Step 4: Convert VirtualFile to PsiDirectory
        PsiDirectory testDirectory = PsiManager.getInstance(project).findDirectory(testDirVF);
        if (testDirectory == null) {
            System.err.println("Failed to find/create test directory.");
            return null;
        }

        // Step 5: Create test file using PSI
        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        PsiFile testFile = fileFactory.createFileFromText(testClassName + ".java", JavaFileType.INSTANCE, content);

        // Step 6: Check if file already exists, overwrite or skip
        PsiFile existingFile = testDirectory.findFile(testClassName + ".java");
        if (existingFile != null) {
            existingFile.delete(); // Optional: Overwrite existing file
        }

        testDirectory.add(testFile);

        // Step 7: Return generated file path
        return testDirectory.getVirtualFile().getPath() + "/" + testClassName + ".java";
    }

    private static VirtualFile createDirectories(VirtualFile root, String[] segments) throws Exception {
        VirtualFile current = root;
        for (String segment : segments) {
            VirtualFile next = current.findChild(segment);
            if (next == null) {
                next = current.createChildDirectory(null, segment);
            }
            current = next;
        }
        return current;
    }*/
/*

    public static String writeTestFile(Project project, PsiFile baseFile, String packageName, String testClassName, String content) {
        PsiDirectory mainDirectory = baseFile.getContainingDirectory();

        // Get the VirtualFile of the baseFile and locate "src/main/java"
        VirtualFile virtualFile = baseFile.getVirtualFile();
        String path = virtualFile.getPath();

        // Replace "src/main/java" with "src/test/java"
        String testPath = path.replace("src/main/java", "src/test/java");
        testPath = testPath.substring(0, testPath.lastIndexOf("/")); // Remove file name

        // Find or create the corresponding test directory
        PsiDirectoryFactory directoryFactory = PsiDirectoryFactory.getInstance(project);
        PsiManager psiManager = PsiManager.getInstance(project);
        VirtualFile testDirVF = createOrFindDirectory(testPath);
        if (testDirVF == null) {
            throw new RuntimeException("Failed to locate or create test directory: " + testPath);
        }

        PsiDirectory testDirectory = psiManager.findDirectory(testDirVF);
        if (testDirectory == null) {
            throw new RuntimeException("Cannot resolve PsiDirectory from: " + testPath);
        }

        PsiFileFactory fileFactory = PsiFileFactory.getInstance(project);
        PsiFile testFile = fileFactory.createFileFromText(testClassName + ".java", JavaFileType.INSTANCE, content);

        // Write inside a write command
        WriteCommandAction.runWriteCommandAction(project, () -> {
            testDirectory.add(testFile);
        });

        return testDirectory.getVirtualFile().getPath() + "/" + testClassName + ".java";
    }

    private static VirtualFile createOrFindDirectory(String path) {
        com.intellij.openapi.vfs.LocalFileSystem localFileSystem = com.intellij.openapi.vfs.LocalFileSystem.getInstance();
        VirtualFile file = localFileSystem.findFileByPath(path);
        if (file == null) {
            file = localFileSystem.refreshAndFindFileByPath(path);
        }
        return file;
    }
*/


    public static String writeTestFile(Project project, PsiFile baseFile, String packageName, String testClassName, String content) {
        PsiDirectory mainDirectory = baseFile.getContainingDirectory();
        VirtualFile virtualFile = baseFile.getVirtualFile();
        String mainPath = virtualFile.getPath();

        // Derive the test directory path
        String testPath = mainPath.replace("src/main/java", "src/test/java");
        testPath = testPath.substring(0, testPath.lastIndexOf("/")); // remove file name

        // Create test directory if it doesn't exist
        VirtualFile testDirVF = createOrFindDirectories(project, testPath);
        if (testDirVF == null) {
            throw new RuntimeException("Failed to locate or create test directory: " + testPath);
        }

        PsiDirectory testDirectory = PsiManager.getInstance(project).findDirectory(testDirVF);
        if (testDirectory == null) {
            throw new RuntimeException("Cannot resolve PsiDirectory from: " + testPath);
        }

        PsiFile testFile = PsiFileFactory.getInstance(project)
                .createFileFromText(testClassName + ".java", JavaFileType.INSTANCE, content);

        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile existingFile = testDirectory.findFile(testClassName + ".java");
            if (existingFile != null) {
                existingFile.delete();
            }
            testDirectory.add(testFile);
        });

        return testDirectory.getVirtualFile().getPath() + "/" + testClassName + ".java";
    }

    private static VirtualFile createOrFindDirectories(Project project, String path) {
        File ioFile = new File(path);
        if (!ioFile.exists()) {
            boolean created = ioFile.mkdirs();
            if (!created) {
                return null;
            }
        }
        LocalFileSystem lfs = LocalFileSystem.getInstance();
        return lfs.refreshAndFindFileByIoFile(ioFile);
    }

}