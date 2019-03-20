package org.bdshadow.generation;

import java.util.List;

import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.generation.BaseCreateMethodsFix;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

public class GenerateDartFromJsonFactoryHandler extends BaseDartGenerateHandler {
    @NotNull
    @Override
    protected BaseCreateMethodsFix createFix(@NotNull DartClass dartClass) {
        return new CreateFromJsonFactoryFix(dartClass);
    }

    @NotNull
    @Override
    protected String getTitle() {
        return "fromJson factory";
    }

    @Override
    protected void collectCandidates(@NotNull DartClass dartClass, @NotNull List<DartComponent> candidates) {
        candidates.addAll(ContainerUtil.findAll(computeClassMembersMap(dartClass, false).values(),
            component -> DartComponentType.typeOf(component) == DartComponentType.FIELD));
    }

    @Override
    protected boolean doAllowEmptySelection() {
        return true;
    }
}
