package com.yourplugin.sparklineageplugin;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;


public class LineageAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || editor == null || psiFile == null) {
            return;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);

        String fullCode = null;

        PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (method == null) {
            Messages.showErrorDialog("Please place the cursor inside a method.", "No Method Found");
            return;
        }
        fullCode = new SparkCodeCollector().collectFullMethodContext(method);

        if (fullCode == null || fullCode.isEmpty()) {
            Messages.showErrorDialog("Could not collect method context.", "Collection Failed");
            return;
        }

        String prompt = PromptBuilder.buildPrompt(fullCode);
        String result = LLMClient.sendPrompt(prompt);
        System.out.println("first result is : "+ result);
        String secondPrompt = "This was the previous response:\n\n" + result +
                "\n\n Mermaid should contain **all the column info** as well. \n" +
                "Give me detailed columns of each dataset** and **mappings till the final dataset for all. \n"+
                "Ensure all quotes are escaped, all column mappings are present, and syntax is correct for mermaid.";
        result=LLMClient.sendPrompt(secondPrompt);
        System.out.println("second result is : "+ result);
        LineageResultPanel.show(project, result);
    }


    @Override
    public void update(AnActionEvent e) {
        PsiElement element = e.getData(CommonDataKeys.PSI_ELEMENT);
        e.getPresentation().setEnabledAndVisible(element instanceof PsiMethod);
    }
}