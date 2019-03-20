package org.bdshadow.generation;

import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateAction;
import com.jetbrains.lang.dart.ide.generation.BaseDartGenerateHandler;
import org.jetbrains.annotations.NotNull;

public class GenerateDartFromJsonFactoryAction extends BaseDartGenerateAction {
    @NotNull
    @Override
    protected BaseDartGenerateHandler getGenerateHandler() {
        return new GenerateDartFromJsonFactoryHandler();
    }
}
