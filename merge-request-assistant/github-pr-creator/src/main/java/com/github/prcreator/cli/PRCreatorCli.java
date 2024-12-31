package com.github.prcreator.cli;

import com.github.prcreator.config.ConfigLoader;
import com.github.prcreator.config.GitHubProperties;
import com.github.prcreator.service.GitHubService;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Slf4j
@Command(
        name = "github-pr-creator",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "Creates a PR with Hello.txt file in a selected GitHub repository."
)
public class PRCreatorCli implements Callable<Integer> {

    @Override
    public Integer call() {
        try {
            GitHubProperties properties = ConfigLoader.loadConfig();
            GitHubService service = GitHubService.create(properties);

            List<GHRepository> repositories = service.listRepositories();
            if (repositories.isEmpty()) {
                log.warn("No repositories found for user: {}", properties.getUsername());
                return 1;
            }

            // Display repositories
            System.out.println("\nAvailable repositories:");
            for (int i = 0; i < repositories.size(); i++) {
                System.out.printf("%d. %s%n", i + 1, repositories.get(i).getName());
            }

            // Get user selection
            GHRepository selectedRepo = getUserSelection(repositories);
            if (selectedRepo == null) {
                return 1;
            }

            // Create PR
            GHPullRequest pr = service.createHelloPR(selectedRepo);
            System.out.println("\nPull request created successfully!");
            System.out.println("URL: " + pr.getHtmlUrl());

            return 0;
        } catch (Exception e) {
            log.error("Error occurred: ", e);
            return 1;
        }
    }

    private GHRepository getUserSelection(List<GHRepository> repositories) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.printf("\nSelect repository (1-%d) or 'q' to quit: ", repositories.size());
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                return null;
            }

            try {
                int selection = Integer.parseInt(input);
                if (selection > 0 && selection <= repositories.size()) {
                    return repositories.get(selection - 1);
                }
            } catch (NumberFormatException ignored) {
                // Invalid input, continue loop
            }
            System.out.println("Invalid selection. Please try again.");
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new PRCreatorCli()).execute(args);
        System.exit(exitCode);
    }
}