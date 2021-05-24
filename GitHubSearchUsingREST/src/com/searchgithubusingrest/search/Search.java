package com.searchgithubusingrest.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The Search Class
 * @author Seham Alharbi
 * Last update 20/05/2021
 */
public class Search {

	private static Gson gson;

	private static String GITHUB_API_BASE_URL = "https://api.github.com/";

	private static String GITHUB_API_SEARCH_CODE_PATH = "search/code?q=";

	private static String GITHUB_API_SEARCH_REPO_PATH = "search/repositories?q=";

	private static int count = 1;

	public static void main(String[] args) throws Exception {

		// To parse or print the resulted response JSON map.
		gson = new GsonBuilder().setPrettyPrinting().create();
		searchForRepos();
	}

	public static void searchRepoCode(String repo) throws ClientProtocolException, IOException {

		String codeContentQuery = "jsoup+in:file,path+language:java+repo:" + repo;

		Map contentSearchResult = makeRESTCall(GITHUB_API_BASE_URL + GITHUB_API_SEARCH_CODE_PATH + codeContentQuery,
				"application/vnd.github.v3.text-match+json");

		System.out.println("\nTotal number of Java Files = " + contentSearchResult.get("total_count") + "\n");
		gson.toJsonTree(contentSearchResult).getAsJsonObject().get("items").getAsJsonArray().forEach(r -> {
			System.out.println("\nFile: " + r.getAsJsonObject().get("name") + "\nRepo: "
					+ r.getAsJsonObject().get("repository").getAsJsonObject().get("html_url") + "\nPath: "
					+ r.getAsJsonObject().get("path"));

			r.getAsJsonObject().get("text_matches").getAsJsonArray()
					.forEach(t -> System.out.println("Matched line: " + t.getAsJsonObject().get("fragment")));
		});
	}

	public static void searchForRepos() throws ClientProtocolException, IOException {

//		String repoQuery = "jsoup+language:java";
		String repoQuery = "SehamAAlharbi/Demo-Repo";
		List<String> repoNames = new ArrayList<String>();

		// When you provide the text-match media type, you will receive an extra key in the JSON payload called text_matches
		// that provides information about the position of your search terms within the text and the property that includes the search term.
		Map repoSearchResult = makeRESTCall(GITHUB_API_BASE_URL + GITHUB_API_SEARCH_REPO_PATH + repoQuery,
				"application/vnd.github.v3.text-match+json");

		System.out.println("Total number of  Repositories= " + repoSearchResult.get("total_count") + "\n");
		gson.toJsonTree(repoSearchResult).getAsJsonObject().get("items").getAsJsonArray().forEach(r -> {
			System.out.println(count + ". Repo Name: " + r.getAsJsonObject().get("full_name"));
			count++;
			repoNames.add(r.getAsJsonObject().get("full_name").toString());

		});

		for (String repositoryFullName : repoNames) {
			searchRepoCode(repositoryFullName.replace("\"", ""));
		}
	}

	/**
	 * This method will make a REST GET call for this URL using Apache http client &
	 * fluent library. Then parse response using GSON & return parsed Map.
	 */
	public static Map makeRESTCall(String restUrl, String acceptHeaderValue)
			throws ClientProtocolException, IOException {
		Request request = Request.Get(restUrl);

		if (acceptHeaderValue != null && !acceptHeaderValue.isBlank()) {
			request.addHeader("Accept", acceptHeaderValue);
		}

		Content content = request.execute().returnContent();
		String jsonString = content.asString();

		// To print response JSON, using GSON
		Map jsonMap = gson.fromJson(jsonString, Map.class);
		return jsonMap;
	}

}
