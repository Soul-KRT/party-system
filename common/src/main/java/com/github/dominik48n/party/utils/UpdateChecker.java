/*
 * Copyright 2023 Dominik48N
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dominik48n.party.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

public class UpdateChecker {

    /**
     * Retrieves the latest version of a GitHub repository's release.
     *
     * @param owner      The owner of the repository.
     * @param repository The name of the repository.
     *
     * @return The latest version as a string.
     *
     * @throws IOException          if an I/O error occurs while making the HTTP request.
     * @throws InterruptedException if the thread is interrupted while waiting for the response.
     */
    public static @NotNull String latestVersion(final @NotNull String owner, final @NotNull String repository) throws IOException, InterruptedException {
        final HttpClient client = HttpClient.newHttpClient();
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.github.com/repos/" + owner + "/" + repository + "/releases/latest"))
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        final HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        final ObjectMapper objectMapper = new ObjectMapper();
        final JsonNode jsonNode = objectMapper.readTree(response.body());
        return jsonNode.get("tag_name").asText();
    }
}
