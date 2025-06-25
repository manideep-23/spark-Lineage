package com.yourplugin.sparklineageplugin;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.HashSet;
import java.util.Set;

public class SparkCodeCollector {
    public String collectFullMethodContext(PsiMethod method) {
        StringBuilder fullCode = new StringBuilder();
        Set<PsiMethod> visited = new HashSet<>();
        collect(method, visited, fullCode);
        return fullCode.toString();
    }

    private void collect(PsiMethod method, Set<PsiMethod> visited, StringBuilder fullCode) {
        if (method == null || visited.contains(method)) return;
        visited.add(method);
        fullCode.append("// Method: ").append(method.getName()).append("\n");
        fullCode.append(method.getText()).append("\n\n");

        method.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                PsiMethod resolved = expression.resolveMethod();
                if (resolved != null) {
                    collect(resolved, visited, fullCode);
                }
            }

            @Override
            public void visitReferenceExpression(PsiReferenceExpression expression) {
                super.visitReferenceExpression(expression);
                PsiElement resolved = expression.resolve();
                if (resolved instanceof PsiField || resolved instanceof PsiVariable) {
                    fullCode.append("// Reference: ").append(expression.getText()).append(" -> ")
                            .append(resolved.getText()).append("\n");
                }
            }
        });
    }
}
