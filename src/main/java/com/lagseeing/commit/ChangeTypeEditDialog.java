package com.lagseeing.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * ChangeType设置弹窗
 *
 * @author FengChen
 */
public final class ChangeTypeEditDialog extends DialogWrapper {

    private final ChangeTypeEditPanel panel;

    ChangeTypeEditDialog(@Nullable final Project project) {
        super(project);
        panel = new ChangeTypeEditPanel();
        setTitle("Edit Change Type");
        setOKButtonText("OK");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel.getMainPanel();
    }

    String getChangeTypeConfig() {
        return panel.getChangeTypeConfig();
    }

}
