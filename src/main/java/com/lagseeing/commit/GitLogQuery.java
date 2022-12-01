package com.lagseeing.commit;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Git日志查询
 */
class GitLogQuery {
    private static final String GIT_LOG_COMMAND = "git log --all --format=%s";
    private static final Pattern COMMIT_FIRST_LINE_FORMAT = Pattern.compile("^[a-z]+\\((.+)\\):.*");

    private final File workingDirectory;

    GitLogQuery(final File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    Result execute() {
        try {
            final ProcessBuilder processBuilder;
            final String osName = System.getProperty("os.name");
            if (osName.contains("Windows")) {
                processBuilder = new ProcessBuilder("cmd", "/C", GIT_LOG_COMMAND);
            } else {
                processBuilder = new ProcessBuilder("sh", "-c", GIT_LOG_COMMAND);
            }

            final Process process = processBuilder.directory(workingDirectory).start();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            final List<String> output = reader.lines().collect(toList());

            process.waitFor(2, TimeUnit.SECONDS);
            process.destroy();
            process.waitFor();

            return new Result(process.exitValue(), output);
        } catch (final Exception e) {
            return Result.ERROR;
        }
    }

    static class Result {
        static Result ERROR = new Result(-1);

        private final int exitValue;
        private final List<String> logs;

        Result(final int exitValue) {
            this(exitValue, emptyList());
        }

        Result(final int exitValue, final List<String> logs) {
            this.exitValue = exitValue;
            this.logs = logs;
        }

        boolean isSuccess() {
            return exitValue == 0;
        }

        public Set<String> getScopes() {
            final Set<String> scopes = new HashSet<>();

            logs.forEach(s -> {
                final Matcher matcher = COMMIT_FIRST_LINE_FORMAT.matcher(s);
                if (matcher.find()) {
                    scopes.add(matcher.group(1));
                }
            });

            return scopes;
        }
    }

}