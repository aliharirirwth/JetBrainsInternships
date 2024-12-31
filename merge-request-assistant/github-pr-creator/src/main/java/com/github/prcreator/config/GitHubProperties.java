package com.github.prcreator.config;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class GitHubProperties {
    private final String token;
    private final String username;

    @Builder.Default
    private final String defaultCommitMessage = "Add Hello.txt file";

    @Builder.Default
    private final String defaultPRTitle = "Add Hello.txt file";

    @Builder.Default
    private final String defaultBranchPrefix = "feature/add-hello-file-";

    @Builder.Default
    private final String defaultFilePath = "Hello.txt";

    @Builder.Default
    private final String defaultFileContent = "Hello world";
}