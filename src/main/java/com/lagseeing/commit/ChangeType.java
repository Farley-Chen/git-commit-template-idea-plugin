package com.lagseeing.commit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * From <a href="https://github.com/commitizen/conventional-commit-types">conventional-commit-types</a>
 *
 * @author Damien Arrachequesne
 * @author FengChen
 */
@Data
public final class ChangeType {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final URL DEFAULT_CHANGE_TYPES_FILE_URL =
        Objects.requireNonNull(ChangeType.class.getClassLoader().getResource("default_change-type.yml"));
    private static final File CHANGE_TYPES_FILE = new File(SystemUtils.getUserHome(),
        "AppData/Roaming/JetBrains/.git-commit-template-idea-plugin/change-type.yml");
    private static List<ChangeType> changeTypesCache;
    public String title;
    public String description;
    public String label;

    public static List<ChangeType> load() {
        try {
            if (changeTypesCache == null) {
                if (!CHANGE_TYPES_FILE.exists()) {
                    FileUtils.copyURLToFile(DEFAULT_CHANGE_TYPES_FILE_URL, CHANGE_TYPES_FILE);
                }
                changeTypesCache = OBJECT_MAPPER.readValue(CHANGE_TYPES_FILE, new TypeReference<>() {
                });
            }
            return changeTypesCache;
        } catch (final IOException e) {
            return List.of();
        }
    }

    public static String loadConfig() {
        try {
            if (!CHANGE_TYPES_FILE.exists()) {
                FileUtils.copyURLToFile(DEFAULT_CHANGE_TYPES_FILE_URL, CHANGE_TYPES_FILE);
            }
            return FileUtils.readFileToString(CHANGE_TYPES_FILE, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            return "";
        }
    }

    public static String loadDefaultConfig() {
        try {
            return IOUtils.toString(DEFAULT_CHANGE_TYPES_FILE_URL, StandardCharsets.UTF_8);
        } catch (final IOException ignored) {
            return "";
        }
    }

    public static void saveConfig(final String config) {
        try {
            changeTypesCache = null;
            FileUtils.writeStringToFile(CHANGE_TYPES_FILE, config, StandardCharsets.UTF_8);
        } catch (final IOException ignored) {
        }
    }

    public static Optional<ChangeType> fromLabel(final String label) {
        return load().stream().filter(it -> it.getLabel().equals(label)).findFirst();
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} - {1}", getLabel(), description);
    }

}
