package com.sgic.defect_tracker.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgic.defect_tracker.dtos.request.CalculateKlocRequestDTO;
import com.sgic.defect_tracker.dtos.response.CalculateKlocResponseDto;
import com.sgic.defect_tracker.entities.ProjectKloc;
import com.sgic.defect_tracker.entities.ProjectDetails;
import com.sgic.defect_tracker.repositories.ProjectDetailsRepository;
import com.sgic.defect_tracker.repositories.ProjectKlocRepository;
import com.sgic.defect_tracker.service.GitHubKlocService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class GitHubKlocServiceImpl implements GitHubKlocService {

    private final ProjectKlocRepository projectKlocRepository;
    private final ProjectDetailsRepository projectDetailsRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.github.com")
            .build();

    private static final Set<String> BACKEND_EXTENSIONS = Set.of(
            ".java"
    );

    private static final Set<String> FRONTEND_EXTENSIONS = Set.of(
            ".js",
            ".jsx",
            ".ts",
            ".tsx"
    );

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "node_modules",
            "target",
            "build",
            "dist",
            ".git",
            "coverage",
            ".next",
            "out"
    );

//    @Override
//    public CalculateKlocResponseDto calculateKloc(
//            Long projectId,
//            CalculateKlocRequestDTO request) {
//
//        // Check project
//        ProjectDetails project = projectDetailsRepository
//                .findById(projectId)
//                .orElseThrow(() ->
//                        new RuntimeException(
//                                "Project not found with id: " + projectId
//                        ));
//
//        // Extract repository information
//        RepositoryInfo backendRepository =
//                parseRepositoryUrl(request.getBackendRepo());
//
//        RepositoryInfo frontendRepository =
//                parseRepositoryUrl(request.getFrontendRepo());
//
//        // Calculate LOC
//        long backendLOC = calculateRepositoryLOC(
//                backendRepository,
//                request.getGithubUsername(),
//                request.getGithubToken(),
//                BACKEND_EXTENSIONS
//        );
//
//        long frontendLOC = calculateRepositoryLOC(
//                frontendRepository,
//                request.getGithubUsername(),
//                request.getGithubToken(),
//                FRONTEND_EXTENSIONS
//        );
//
//        // Convert LOC to KLOC
//        double backendKLOC = backendLOC / 1000.0;
//        double frontendKLOC = frontendLOC / 1000.0;
//
//        double totalKLOC = backendKLOC + frontendKLOC;
//
//        // Minimum KLOC = 0.1
//        if (totalKLOC < 0.1) {
//            totalKLOC = 0.1;
//        }
//
//        // Save KLOC for selected project
//        ProjectKloc projectKloc =
//                projectKlocRepository
//                        .findByProjectDetails_ProjectId(projectId)
//                        .orElseGet(ProjectKloc::new);
//
//        projectKloc.setProjectDetails(project);
//        projectKloc.setKiloOfCode(totalKLOC);
//
//        projectKlocRepository.save(projectKloc);
//
//        return new CalculateKlocResponseDto(
//                backendLOC,
//                frontendLOC,
//                backendKLOC,
//                frontendKLOC,
//                totalKLOC
//        );
//    }

    @Override
    public CalculateKlocResponseDto calculateKloc(
            Long projectId,
            CalculateKlocRequestDTO request) {

        // Check project
        ProjectDetails project = projectDetailsRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Project not found with id: " + projectId
                        ));

        String backendRepoUrl = request.getBackendRepo();
        String frontendRepoUrl = request.getFrontendRepo();

        // At least one repository is required
        if ((backendRepoUrl == null || backendRepoUrl.isBlank())
                && (frontendRepoUrl == null || frontendRepoUrl.isBlank())) {

            throw new IllegalArgumentException(
                    "At least one repository URL is required"
            );
        }

        long backendLOC = 0;
        long frontendLOC = 0;

        /*
         * CASE 1:
         * Backend repository exists
         */
        if (backendRepoUrl != null && !backendRepoUrl.isBlank()) {

            RepositoryInfo backendRepository =
                    parseRepositoryUrl(backendRepoUrl);

            backendLOC = calculateRepositoryLOC(
                    backendRepository,
                    request.getGithubUsername(),
                    request.getGithubToken(),
                    BACKEND_EXTENSIONS
            );
        }

        /*
         * CASE 2:
         * Frontend repository exists
         */
        if (frontendRepoUrl != null && !frontendRepoUrl.isBlank()) {

            /*
             * If backend and frontend use the SAME repository,
             * don't scan the repository twice.
             *
             * In this case, scan the same repository separately
             * for backend and frontend extensions.
             */
            RepositoryInfo frontendRepository =
                    parseRepositoryUrl(frontendRepoUrl);

            frontendLOC = calculateRepositoryLOC(
                    frontendRepository,
                    request.getGithubUsername(),
                    request.getGithubToken(),
                    FRONTEND_EXTENSIONS
            );
        }

        // Convert LOC to KLOC
        double backendKLOC =
                backendLOC / 1000.0;

        double frontendKLOC =
                frontendLOC / 1000.0;

        double totalKLOC =
                backendKLOC + frontendKLOC;

        // Minimum KLOC = 0.1
        if (totalKLOC < 0.1) {
            totalKLOC = 0.1;
        }

        // Save KLOC for selected project
        ProjectKloc projectKloc =
                projectKlocRepository
                        .findByProjectDetails_ProjectId(projectId)
                        .orElseGet(ProjectKloc::new);

        projectKloc.setProjectDetails(project);
        projectKloc.setKiloOfCode(totalKLOC);

        projectKlocRepository.save(projectKloc);

        return new CalculateKlocResponseDto(
                backendLOC,
                frontendLOC,
                backendKLOC,
                frontendKLOC,
                totalKLOC
        );
    }
    private long calculateRepositoryLOC(
            RepositoryInfo repository,
            String username,
            String token,
            Set<String> allowedExtensions) {

        String branchUrl =
                "/repos/" +
                        repository.owner() +
                        "/" +
                        repository.repo();

        String repoResponse = restClient.get()
                .uri(branchUrl)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .header(
                        HttpHeaders.ACCEPT,
                        "application/vnd.github+json"
                )
                .retrieve()
                .body(String.class);

        try {

            JsonNode repoRoot =
                    objectMapper.readTree(repoResponse);

            String defaultBranch =
                    repoRoot.path("default_branch").asText();

            if (defaultBranch == null || defaultBranch.isBlank()) {
                throw new RuntimeException(
                        "Default branch not found for repository: "
                                + repository.repo()
                );
            }

            String treeUrl =
                    "/repos/" +
                            repository.owner() +
                            "/" +
                            repository.repo() +
                            "/git/trees/" +
                            defaultBranch +
                            "?recursive=1";

            String response = restClient.get()
                    .uri(treeUrl)
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer " + token
                    )
                    .header(
                            HttpHeaders.ACCEPT,
                            "application/vnd.github+json"
                    )
                    .retrieve()
                    .body(String.class);

            JsonNode root =
                    objectMapper.readTree(response);

            JsonNode tree = root.get("tree");

            if (tree == null || !tree.isArray()) {
                return 0;
            }

            long totalLOC = 0;

            for (JsonNode item : tree) {

                String type =
                        item.path("type").asText();

                String path =
                        item.path("path").asText();

                if (!"blob".equals(type)) {
                    continue;
                }

                if (shouldExclude(path)) {
                    continue;
                }

                if (!hasAllowedExtension(
                        path,
                        allowedExtensions)) {
                    continue;
                }

                totalLOC += getFileLOC(
                        repository.owner(),
                        repository.repo(),
                        path,
                        token
                );
            }

            return totalLOC;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to calculate LOC for repository: "
                            + repository.repo(),
                    e
            );
        }
    }
//    private long calculateRepositoryLOC(
//            RepositoryInfo repository,
//            String username,
//            String token,
//            Set<String> allowedExtensions) {
//
//        String treeUrl =
//                "/repos/" +
//                        repository.owner() +
//                        "/" +
//                        repository.repo() +
//                        "/git/trees/HEAD?recursive=1";
//
//        String response = restClient.get()
//                .uri(treeUrl)
//                .header(
//                        HttpHeaders.AUTHORIZATION,
//                        "Bearer " + token
//                )
//                .header(
//                        HttpHeaders.ACCEPT,
//                        "application/vnd.github+json"
//                )
//                .retrieve()
//                .body(String.class);
//
//        try {
//
//            JsonNode root = objectMapper.readTree(response);
//
//            JsonNode tree = root.get("tree");
//
//            if (tree == null || !tree.isArray()) {
//                return 0;
//            }
//
//            long totalLOC = 0;
//
//            for (JsonNode item : tree) {
//
//                String type = item.path("type").asText();
//                String path = item.path("path").asText();
//
//                if (!"blob".equals(type)) {
//                    continue;
//                }
//
//                if (shouldExclude(path)) {
//                    continue;
//                }
//
//                if (!hasAllowedExtension(path, allowedExtensions)) {
//                    continue;
//                }
//
//                totalLOC += getFileLOC(
//                        repository.owner(),
//                        repository.repo(),
//                        path,
//                        token
//                );
//            }
//
//            return totalLOC;
//
//        } catch (Exception e) {
//            throw new RuntimeException(
//                    "Failed to calculate LOC for repository: "
//                            + repository.repo(),
//                    e
//            );
//        }
//    }

    private long getFileLOC(
            String owner,
            String repo,
            String path,
            String token) {

        String url =
                "/repos/" +
                        owner +
                        "/" +
                        repo +
                        "/contents/" +
                        path;

        String response = restClient.get()
                .uri(url)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .header(
                        HttpHeaders.ACCEPT,
                        "application/vnd.github+json"
                )
                .retrieve()
                .body(String.class);

        try {

            JsonNode root = objectMapper.readTree(response);

            String encodedContent =
                    root.path("content").asText();

            if (encodedContent == null ||
                    encodedContent.isBlank()) {
                return 0;
            }

            // GitHub content response is Base64 encoded
            String cleanedContent =
                    encodedContent.replaceAll("\\s", "");

            byte[] decoded =
                    java.util.Base64.getDecoder()
                            .decode(cleanedContent);

            String content =
                    new String(
                            decoded,
                            java.nio.charset.StandardCharsets.UTF_8
                    );

            if (content.isBlank()) {
                return 0;
            }

            return content.lines().count();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to read file: " + path,
                    e
            );
        }
    }

    private boolean shouldExclude(String path) {

        String normalizedPath =
                path.replace("\\", "/");

        for (String excluded : EXCLUDED_PATHS) {

            if (normalizedPath.contains(
                    "/" + excluded + "/")
                    || normalizedPath.startsWith(
                    excluded + "/")) {

                return true;
            }
        }

        return false;
    }

    private boolean hasAllowedExtension(
            String path,
            Set<String> extensions) {

        String lowerCasePath =
                path.toLowerCase();

        return extensions.stream()
                .anyMatch(lowerCasePath::endsWith);
    }

    private RepositoryInfo parseRepositoryUrl(
            String repositoryUrl) {

        if (repositoryUrl == null ||
                repositoryUrl.isBlank()) {

            throw new IllegalArgumentException(
                    "Repository URL is required"
            );
        }

        String url = repositoryUrl
                .trim()
                .replace(".git", "");

        if (url.endsWith("/")) {
            url = url.substring(
                    0,
                    url.length() - 1
            );
        }

        String prefix =
                "https://github.com/";

        if (!url.startsWith(prefix)) {

            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL: "
                            + repositoryUrl
            );
        }

        String repositoryPath =
                url.substring(prefix.length());

        String[] parts =
                repositoryPath.split("/");

        if (parts.length < 2) {

            throw new IllegalArgumentException(
                    "Invalid GitHub repository URL: "
                            + repositoryUrl
            );
        }

        return new RepositoryInfo(
                parts[0],
                parts[1]
        );
    }

    private record RepositoryInfo(
            String owner,
            String repo
    ) {
    }
}