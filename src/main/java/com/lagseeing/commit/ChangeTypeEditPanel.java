package com.lagseeing.commit;

import lombok.Getter;

import javax.swing.*;

/**
 * ChangeType设置面板
 *
 * @author FengChen
 */
public final class ChangeTypeEditPanel {
    private JTextArea editTextArea;
    @Getter
    private JPanel mainPanel;
    private JButton resetToDefaultButton;

    ChangeTypeEditPanel() {
        editTextArea.setText(ChangeType.loadConfig());
        resetToDefaultButton.addActionListener(e -> editTextArea.setText(ChangeType.loadDefaultConfig()));
    }

    String getChangeTypeConfig() {
        return editTextArea.getText();
    }

}
