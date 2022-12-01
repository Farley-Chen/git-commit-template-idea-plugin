package com.lagseeing.commit;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.uiDesigner.core.GridConstraints;
import org.apache.batik.ext.swing.JGridBagPanel;
import org.apache.commons.collections.EnumerationUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Optional;

/**
 * 插件主面板
 *
 * @author Damien Arrachequesne
 */
public class CommitPanel {
    private JPanel mainPanel;
    private JComboBox<String> changeScope;
    private JTextField shortDescription;
    private JTextArea longDescription;
    private JTextArea breakingChanges;
    private JTextField closedIssues;
    private JCheckBox wrapTextCheckBox;
    private JCheckBox skipCICheckBox;
    private JPanel changeTypePanel;
    private JButton changeTypeEditButton;
    private ButtonGroup changeTypeGroup;

    CommitPanel(final Project project, final CommitMessage commitMessage) {
        // 1. 查询Git日志，填充scope下拉框
        final File workingDirectory = new File(project.getBasePath());
        final GitLogQuery.Result result = new GitLogQuery(workingDirectory).execute();
        if (result.isSuccess()) {
            // no value by default
            changeScope.addItem("");
            result.getScopes().forEach(changeScope::addItem);
        }
        // 2. 载入ChangeType面板
        loadChangeTypePanel(false);
        if (commitMessage != null) {
            // 3. 从提交信息 还原 插件主面板 设置
            restoreValuesFromParsedCommitMessage(commitMessage);
        }

        changeTypeEditButton.addActionListener(actionEvent -> {
            // ChangeType设置弹窗
            final var dialog = new ChangeTypeEditDialog(null);
            dialog.show();

            if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
                // 提交ChangeType设置弹窗时，保存ChangeType设置
                ChangeType.saveConfig(dialog.getChangeTypeConfig());
                // 重新载入ChangeType面板
                loadChangeTypePanel(true);
            }
        });
    }

    /**
     * 载入ChangeType面板
     *
     * @param updateUI 是否更新界面
     */
    private void loadChangeTypePanel(final boolean updateUI) {
        changeTypePanel.removeAll();
        EnumerationUtils.toList(changeTypeGroup.getElements()).forEach(b -> changeTypeGroup.remove((AbstractButton)b));
        final var changeTypeBagPanel = new JGridBagPanel();
        for (final var changeType : ChangeType.load()) {
            final var changeTypeButton = new JRadioButton();
            changeTypeButton.setActionCommand(changeType.getLabel());
            changeTypeButton.setText(
                MessageFormat.format("{0} - {1}", changeType.getLabel(), changeType.getDescription()));
            final var gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            changeTypeBagPanel.add(changeTypeButton, gridBagConstraints);
            changeTypeGroup.add(changeTypeButton);
        }
        changeTypePanel.add(changeTypeBagPanel, new GridConstraints());
        if (updateUI) {
            changeTypePanel.updateUI();
        }
    }

    JPanel getMainPanel() {
        return mainPanel;
    }

    CommitMessage getCommitMessage() {
        return new CommitMessage(getSelectedChangeType().orElse(null), (String)changeScope.getSelectedItem(),
            shortDescription.getText().trim(), longDescription.getText().trim(), breakingChanges.getText().trim(),
            closedIssues.getText().trim(), wrapTextCheckBox.isSelected(), skipCICheckBox.isSelected());
    }

    private Optional<ChangeType> getSelectedChangeType() {
        for (final Enumeration<AbstractButton> buttons = changeTypeGroup.getElements(); buttons.hasMoreElements(); ) {
            final AbstractButton button = buttons.nextElement();
            if (button.isSelected()) {
                return ChangeType.fromLabel(button.getActionCommand());
            }
        }
        return Optional.empty();
    }

    /**
     * 从提交信息 还原 插件主面板 设置
     *
     * @param commitMessage 提交信息
     */
    private void restoreValuesFromParsedCommitMessage(final CommitMessage commitMessage) {
        if (commitMessage.getChangeType() != null) {
            for (final Enumeration<AbstractButton> buttons = changeTypeGroup.getElements();
                buttons.hasMoreElements(); ) {
                final AbstractButton button = buttons.nextElement();

                if (button.getActionCommand().equalsIgnoreCase(commitMessage.getChangeType().getLabel())) {
                    button.setSelected(true);
                }
            }
        }
        changeScope.setSelectedItem(commitMessage.getChangeScope());
        shortDescription.setText(commitMessage.getShortDescription());
        longDescription.setText(commitMessage.getLongDescription());
        breakingChanges.setText(commitMessage.getBreakingChanges());
        closedIssues.setText(commitMessage.getClosedIssues());
        skipCICheckBox.setSelected(commitMessage.isSkipCI());
    }
}
