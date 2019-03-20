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

public class CreateFromJsonFactoryFix extends BaseCreateMethodsFix<DartComponent> {

    public CreateFromJsonFactoryFix(@NotNull DartClass dartClass) {
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

        template.addTextSegment("factory ");
        template.addTextSegment(this.myDartClass.getName());
        template.addTextSegment(".fromJson");
        template.addTextSegment("(Map<String, dynamic> json)");
        template.addTextSegment(" {");
        template.addTextSegment("return ");
        template.addTextSegment(this.myDartClass.getName());
        template.addTextSegment("(");

        for (Iterator<DartComponent> iterator = elementsToProcess.iterator(); iterator.hasNext(); ) {
            DartComponent component = iterator.next();
            template.addTextSegment(component.getName());
            template.addTextSegment(": ");


            DartReturnType returnType = PsiTreeUtil.getChildOfType(component, DartReturnType.class);
            DartType dartType = PsiTreeUtil.getChildOfType(component, DartType.class);
            String typeText = returnType == null ? DartPresentableUtil.buildTypeText(component, dartType, null) : DartPresentableUtil.buildTypeText(component, returnType, null);

            boolean isGenericCollection = typeText.startsWith("Set") || typeText.startsWith("List");

            if (isGenericCollection) {
                addCollection(template, component, typeText);
                template.addTextSegment(",");
                continue;
            }

            switch (typeText) {
                case "int":
                case "double":
                case "DateTime": {
                    template.addTextSegment(typeText);
                    template.addTextSegment(".parse(");
                    addJsonRetrieval(template, component);
                    template.addTextSegment(")");
                    break;
                }
                case "bool": {
                    addJsonRetrieval(template, component);
                    template.addTextSegment(".toLowerCase() == 'true'");
                    break;
                }
                case "": //var
                case "String": {
                    addJsonRetrieval(template, component);
                    break;
                }
                default:
                    template.addTextSegment(typeText);
                    template.addTextSegment(".fromJson(");
                    addJsonRetrieval(template, component);
                    template.addTextSegment(")");

            }
            template.addTextSegment(",");
        }
        template.addTextSegment(");");
        template.addTextSegment("}\n");

        template.addEndVariable();
        template.addTextSegment(" "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed
        return template;
    }

    private void addCollection(Template template, DartComponent component, String typeText) {
        int genericBracketIndex = typeText.indexOf("<");
        String collectionType = genericBracketIndex == -1 ? typeText : typeText.substring(0, genericBracketIndex);
        String genericType = genericBracketIndex == -1 ? "" : typeText.substring(genericBracketIndex + 1, typeText.lastIndexOf(">"));
        template.addTextSegment(collectionType);
        template.addTextSegment(".of(");
        addJsonRetrieval(template, component);
        template.addTextSegment(").map((i) => ");

        switch (genericType) {
            case "int":
            case "double":
            case "DateTime": {
                template.addTextSegment(genericType);
                template.addTextSegment(".parse(i)");
                break;
            }
            case "bool": {
                template.addTextSegment("i.toLowerCase() == 'true'");
                break;
            }
            case "String": {
                addJsonRetrieval(template, component);
                break;
            }
            case "":
            default:
                template.addTextSegment("i /* can't generate it properly yet */");
        }
        template.addTextSegment(").to");
        template.addTextSegment(collectionType);
        template.addTextSegment("()");
    }

    /**
     * Adds <code>json["componentName"] to the template</code>
     * @param template
     * @param component
     */
    private void addJsonRetrieval(Template template, DartComponent component) {
        template.addTextSegment("json[\"");
        template.addTextSegment(component.getName());
        template.addTextSegment("\"]");
    }

    @Nullable
    @Override
    protected Template buildFunctionsText(TemplateManager templateManager, DartComponent dartComponent) {
        //ignore
        return null;
    }
}
