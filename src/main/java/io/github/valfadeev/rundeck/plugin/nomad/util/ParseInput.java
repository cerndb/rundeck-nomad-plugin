package io.github.valfadeev.rundeck.plugin.nomad.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.dtolabs.rundeck.core.storage.ResourceMeta;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;

import org.rundeck.storage.api.PathUtil;

public class ParseInput {
    public static String[] checkedSplit(String input, String delimiter) {
        String[] result = input.split(delimiter);
        if (result.length > 1) {
            return result;
        } else {
            throw new IllegalArgumentException(
                    String.format("valid input must be a string "
                                + "delimited by \"%s\", got: \"%s\"",
                            delimiter, input));
        }

    }
    public static Map<String, String> kvToMap(String input) {
        return Arrays.stream(input.split("\n"))
                .map(s -> checkedSplit(s, "="))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
    }

    public static String getPWFromStorage(final PluginStepContext context, String path) {

        ResourceMeta contents = context.getExecutionContext().getStorageTree()
            .getResource(PathUtil.asPath(path)).getContents();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            contents.writeContent(byteArrayOutputStream);
          } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        return new String(byteArrayOutputStream.toByteArray());
    }

}
