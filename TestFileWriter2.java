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
import com.intellij.openapi.ui.Messages;
import java.io.IOException;
public class TestFileWriter2 {

    public static String writeTestFile(Project project, PsiFile baseFile, String packageName, String testClassName, String content) {
        PsiDirectory mainDirectory = baseFile.getContainingDirectory();
        VirtualFile virtualFile = baseFile.getVirtualFile();
        String mainPath = virtualFile.getPath();

        // Derive the test directory path based on package
        String baseTestPath = mainPath.replace("src/main/java", "src/test/java");
        baseTestPath = baseTestPath.substring(0, baseTestPath.lastIndexOf("/"));

        // Create full path including package folder
        String packagePath = packageName.replace('.', '/');
        String fullTestPath = baseTestPath.substring(0, baseTestPath.indexOf("src/test/java") + "src/test/java".length()) + "/" + packagePath;

        VirtualFile testDirVF = createOrFindDirectories(project, fullTestPath);
        if (testDirVF == null) {
            throw new RuntimeException("Failed to locate or create test directory: " + fullTestPath);
        }

        PsiDirectory testDirectory = PsiManager.getInstance(project).findDirectory(testDirVF);
        if (testDirectory == null) {
            throw new RuntimeException("Cannot resolve PsiDirectory from: " + fullTestPath);
        }

        String finalTestClassName = testClassName;
        String fileName = finalTestClassName + ".java";
        PsiFile existingFile = testDirectory.findFile(fileName);

        if (existingFile != null) {
            int result = Messages.showYesNoCancelDialog(
                    "Test file already exists. Do you want to overwrite it?",
                    "Test File Exists",
                    "Overwrite",
                    "Create New",
                    "Cancel",
                    Messages.getQuestionIcon()
            );

            if (result == Messages.CANCEL) {
                return null;
            } else if (result == Messages.NO) {
                // Create new with incrementing suffix
                int suffix = 1;
                while (testDirectory.findFile(testClassName + suffix + ".java") != null) {
                    suffix++;
                }
                finalTestClassName = testClassName + suffix;
                fileName = finalTestClassName + ".java";
            }
        }

        String finalContent = content.replaceFirst("class\\s+" + testClassName, "class " + finalTestClassName);

        PsiFile testFile = PsiFileFactory.getInstance(project)
                .createFileFromText(fileName, JavaFileType.INSTANCE, finalContent);

        String finalFileName = fileName;
        WriteCommandAction.runWriteCommandAction(project, () -> {
            PsiFile fileToOverwrite = testDirectory.findFile(finalFileName);
            if (fileToOverwrite != null) {
                fileToOverwrite.delete();
            }
            testDirectory.add(testFile);
        });

        return testDirectory.getVirtualFile().getPath() + "/" + finalFileName;
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
