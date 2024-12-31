package com.github.prcreator.service;

import com.github.prcreator.config.GitHubProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class GitHubService {
    private final GitHub github;
    private final GitHubProperties properties;

    public static GitHubService create(GitHubProperties properties) throws IOException {
        log.info("Creating GitHub service for user: {}", properties.getUsername());
        GitHub github = new GitHubBuilder()
                .withOAuthToken(properties.getToken())
                .build();

        // Verify authentication and permissions
        GHMyself myself = github.getMyself();
        log.info("Successfully authenticated as: {}", myself.getLogin());

        return new GitHubService(github, properties);
    }

    public List<GHRepository> listRepositories() throws IOException {
        log.info("Fetching repositories for user: {}", properties.getUsername());

        // Get authenticated user's repositories (includes private repos)
        GHMyself myself = github.getMyself();
        Map<String, GHRepository> repos = myself.getAllRepositories();

        log.info("Found {} repositories", repos.size());

        // Log each repository's information
        for (Map.Entry<String, GHRepository> entry : repos.entrySet()) {
            String name = entry.getKey();
            GHRepository repo = entry.getValue();
            boolean isPrivate = repo.isPrivate();
            log.info("Repository: {} (Private: {})", name, isPrivate);
        }

        return new ArrayList<>(repos.values());
    }

    public GHPullRequest createHelloPR(GHRepository repository) throws IOException {
        log.info("Creating PR in repository: {} (Private: {})",
                repository.getName(), repository.isPrivate());

        // Create new branch
        String defaultBranch = repository.getDefaultBranch();
        String branchName = properties.getDefaultBranchPrefix() + System.currentTimeMillis();

        log.debug("Creating branch: {}", branchName);
        GHRef mainRef = repository.getRef("heads/" + defaultBranch);
        repository.createRef("refs/heads/" + branchName, mainRef.getObject().getSha());

        // Create file
        log.debug("Creating file: {}", properties.getDefaultFilePath());
        repository.createContent()
                .branch(branchName)
                .path(properties.getDefaultFilePath())
                .content(properties.getDefaultFileContent())
                .message(properties.getDefaultCommitMessage())
                .commit();

        // Create PR
        log.debug("Creating pull request");
        GHPullRequest pullRequest = repository.createPullRequest(
                properties.getDefaultPRTitle(),
                branchName,
                defaultBranch,
                "This PR adds a Hello.txt file with 'Hello world' content."
        );

        log.info("Pull request created: {}", pullRequest.getHtmlUrl());
        return pullRequest;
    }
}