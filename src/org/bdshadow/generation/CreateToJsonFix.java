package org.bdshadow.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartReturnType;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartPresentableUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.Set;

public class CreateToJsonFix extends BaseCreateMethodsFix<DartComponent> {

    public CreateToJsonFix(@NotNull DartClass dartClass) {
        super(dartClass);
    }

    @Override
    protected void processElements(@NotNull final Project project,
                                   @NotNull final Editor editor,
                                   @NotNull final Set<DartComponent> elementsToProcess) {
        final TemplateManager templateManager = TemplateManager.getInstance(project);
        anchor = doAddMethodsForOne(editor, templateManager, buildFunctionsText(templateManager, elementsToProcess), anchor);
    }

    @NotNull
    @Override
    protected String getNothingFoundMessage() {
        return ""; // can't be called actually because processElements() is overridden
    }

    protected Template buildFunctionsText(TemplateManager templateManager, Set<DartComponent> elementsToProcess) {
        final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
        template.setToReformat(true);

        template.addTextSegment("Map<String, dynamic> toJson() {");
        template.addTextSegment("return {");

        for (Iterator<DartComponent> iterator = elementsToProcess.iterator(); iterator.hasNext(); ) {
            DartComponent component = iterator.next();

            template.addTextSegment("\"");
            template.addTextSegment(component.getName());
            template.addTextSegment("\": ");

            DartReturnType returnType = PsiTreeUtil.getChildOfType(component, DartReturnType.class);
            DartType dartType = PsiTreeUtil.getChildOfType(component, DartType.class);
            String typeText = returnType == null ? DartPresentableUtil.buildTypeText(component, dartType, null) : DartPresentableUtil.buildTypeText(component, returnType, null);

            boolean isGenericCollection = typeText.startsWith("Set") || typeText.startsWith("List");
            if (isGenericCollection) {
                template.addTextSegment("jsonEncode(");
                template.addTextSegment("this.");
                template.addTextSegment(component.getName());
                template.addTextSegment(")");
                template.addTextSegment(",");
                continue;
            }

            template.addTextSegment("this.");
            switch (typeText) {
                case "DateTime": {
                    template.addTextSegment(component.getName());
                    template.addTextSegment(".toIso8601String()");
                    break;
                }
                default:
                    template.addTextSegment(component.getName());
            }
            template.addTextSegment(",");
        }
        template.addTextSegment("}");
        template.addTextSegment("}");
        template.addEndVariable();
        template.addTextSegment(" "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
        return template;
    }

    @Nullable
    @Override
    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent dartComponent) {
        //ignore
        return null;
    }
}
