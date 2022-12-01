package com.lagseeing.commit;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author Damien Arrachequesne
 */
public class CreateCommitAction extends AnAction implements DumbAware {

    /**
     * 获取Git提交页面面板
     */
    private static Optional<CommitMessageI> getCommitPanel(final AnActionEvent actionEvent) {
        final var data = Refreshable.PANEL_KEY.getData(actionEvent.getDataContext());
        if (data instanceof CommitMessageI) {
            return Optional.of((CommitMessageI)data);
        }
        return Optional.ofNullable(VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(actionEvent.getDataContext()));
    }

    @Override
    public void actionPerformed(@NotNull final AnActionEvent actionEvent) {
        // 1. 解析Git提交面板中的信息
        final var commitPanel = getCommitPanel(actionEvent);
        if (commitPanel.isEmpty()) {
            return;
        }
        final CommitMessage commitMessage = parseExistingCommitMessage(commitPanel.get()).orElse(null);
        // 2. 生成插件主弹窗
        final CommitDialog dialog = new CommitDialog(actionEvent.getProject(), commitMessage);
        dialog.show();

        if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
            // 3. 提交插件主弹窗时，设置Git提交面板中的信息
            commitPanel.get().setCommitMessage(dialog.getCommitMessage().toString());
        }

    }

    /**
     * 解析已有的提交信息
     *
     * @param commitPanel 提交面板
     * @return 提交信息
     */
    private Optional<CommitMessage> parseExistingCommitMessage(final CommitMessageI commitPanel) {
        if (!(commitPanel instanceof CheckinProjectPanel)) {
            return Optional.empty();
        }
        final var commitMessageString = ((CheckinProjectPanel)commitPanel).getCommitMessage();
        return Optional.of(CommitMessage.parse(commitMessageString));
    }

}
