package com.lagseeing.commit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author Damien Arrachequesne <damien.arrachequesne@gmail.com>
 */
class CommitMessage {
    public static final Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^([a-z]+)(\\((.+)\\))?: (.+)");
    public static final Pattern COMMIT_CLOSES_FORMAT = Pattern.compile("Closes (.+)");
    /**
     * <a href="https://stackoverflow.com/a/2120040/5138796">how-to-wrap-git-commit-comments</a>
     */
    private static final int MAX_LINE_LENGTH = 72;
    private ChangeType changeType;
    private String changeScope, shortDescription, longDescription, breakingChanges, closedIssues;
    private boolean wrapText = true;
    private boolean skipCI = false;

    private CommitMessage() {
        longDescription = "";
        breakingChanges = "";
        closedIssues = "";
    }

    public CommitMessage(final ChangeType changeType, final String changeScope, final String shortDescription,
        final String longDescription, final String breakingChanges, final String closedIssues, final boolean wrapText,
        final boolean skipCI) {
        this.changeType = changeType;
        this.changeScope = changeScope;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.breakingChanges = breakingChanges;
        this.closedIssues = closedIssues;
        this.wrapText = wrapText;
        this.skipCI = skipCI;
    }

    /**
     * 解析提交信息
     *
     * @param message 提交信息字符串
     * @return 提交信息
     */
    public static CommitMessage parse(final String message) {
        final var commitMessage = new CommitMessage();

        try {
            var matcher = COMMIT_FIRST_LINE_FORMAT.matcher(message);
            if (!matcher.find()) {
                return commitMessage;
            }

            commitMessage.changeType = ChangeType.fromLabel(matcher.group(1)).orElseThrow();
            commitMessage.changeScope = matcher.group(3);
            commitMessage.shortDescription = matcher.group(4);

            final var strings = message.split("\n");
            if (strings.length < 2) {
                return commitMessage;
            }

            int pos = 1;
            StringBuilder stringBuilder;

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                final var lineString = strings[pos];
                if (lineString.startsWith("BREAKING") || lineString.startsWith(
                    "Closes") || "[skip ci]".equalsIgnoreCase(lineString)) {
                    break;
                }
                stringBuilder.append(lineString).append('\n');
            }
            commitMessage.longDescription = stringBuilder.toString().trim();

            stringBuilder = new StringBuilder();
            for (; pos < strings.length; pos++) {
                final var lineString = strings[pos];
                if (lineString.startsWith("Closes") || "[skip ci]".equalsIgnoreCase(lineString)) {
                    break;
                }
                stringBuilder.append(lineString).append('\n');
            }
            commitMessage.breakingChanges = stringBuilder.toString().trim().replace("BREAKING CHANGE: ", "");

            matcher = COMMIT_CLOSES_FORMAT.matcher(message);
            stringBuilder = new StringBuilder();
            while (matcher.find()) {
                stringBuilder.append(matcher.group(1)).append(',');
            }
            if (stringBuilder.length() > 0) {
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            }
            commitMessage.closedIssues = stringBuilder.toString();

            commitMessage.skipCI = message.contains("[skip ci]");
        } catch (final RuntimeException ignored) {
        }

        return commitMessage;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(changeType.getLabel());
        if (isNotBlank(changeScope)) {
            builder.append('(').append(changeScope).append(')');
        }
        builder.append(": ").append(shortDescription);

        if (isNotBlank(longDescription)) {
            builder.append(System.lineSeparator()).append(System.lineSeparator())
                .append(wrapText ? WordUtils.wrap(longDescription, MAX_LINE_LENGTH) : longDescription);
        }

        if (isNotBlank(breakingChanges)) {
            final String content = "BREAKING CHANGE: " + breakingChanges;
            builder.append(System.lineSeparator()).append(System.lineSeparator())
                .append(wrapText ? WordUtils.wrap(content, MAX_LINE_LENGTH) : content);
        }

        if (isNotBlank(closedIssues)) {
            builder.append(System.lineSeparator());
            for (final String closedIssue : closedIssues.split(",")) {
                builder.append(System.lineSeparator()).append("Closes ").append(formatClosedIssue(closedIssue));
            }
        }

        if (skipCI) {
            builder.append(System.lineSeparator()).append(System.lineSeparator()).append("[skip ci]");
        }

        return builder.toString();
    }

    private String formatClosedIssue(final String closedIssue) {
        final String trimmed = closedIssue.trim();
        return (StringUtils.isNumeric(trimmed) ? "#" : "") + trimmed;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public String getChangeScope() {
        return changeScope;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getBreakingChanges() {
        return breakingChanges;
    }

    public String getClosedIssues() {
        return closedIssues;
    }

    public boolean isSkipCI() {
        return skipCI;
    }
}