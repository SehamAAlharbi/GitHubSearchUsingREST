package com.searchgithubusingrest.search;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
	
	private static String GITHUB_API_REPO_Content_PATH = "repos/";

	private static int count = 1;

	public static void main(String[] args) throws Exception {

		// To parse or print the resulted response JSON map to be used.
		gson = new GsonBuilder().setPrettyPrinting().create();
		searchForRepos();
	}
	
	/**
	 * This method takes a repository and list all of its java files that make use of Jsoup library.
	 * @param repo is the repository to search its code
	 * @throws ClientProtocolException
	 * @throws IOException
	 */

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
	
	/**
	 * This method searches for repositories that uses Jsoup library,
	 * and calls the searchRepoCode method to get the code that make use of Jsoup.
	 * @throws ClientProtocolException
	 * @throws IOException
	 */

	public static void searchForRepos() throws ClientProtocolException, IOException {

//		String repoQuery = "jsoup+language:java";
		String repoQuery = "SehamAAlharbi+AND+Jsoup+in:name";

		// When you provide the text-match media type, you will receive an extra key in the JSON payload called text_matches
		// that provides information about the position of your search terms within the text and the property that includes the search term.
		Map repoSearchResult = makeRESTCall(GITHUB_API_BASE_URL + GITHUB_API_SEARCH_REPO_PATH + repoQuery,
				"application/vnd.github.v3.text-match+json");

		System.out.println("\nTotal number of  Repositories= " + repoSearchResult.get("total_count") + "\n");
		gson.toJsonTree(repoSearchResult).getAsJsonObject().get("items").getAsJsonArray().forEach(r -> {
			System.out.println("\n" + count + ". Repo Name: " + r.getAsJsonObject().get("full_name"));
			count++;
			try {
				searchRepoCode(r.getAsJsonObject().get("full_name").toString().replace("\"", ""));
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
		
//		getFileContent (repoSearchResult);

	}
	
	/**
	 * This method will print out the content of the extracted java files 
	 */
	public static void getFileContent (String repoName, String ) {
		
		/*
		 * Call GitHub REST API - https://developer.github.com/v3/repos/contents/
		 * 
		 * Using Spring's RestTemplate to simplify REST call. Any other REST client
		 * library can be used here.
		 */
		
	
		List<Map> response = new ArrayList<Map> ();
		response.add(makeRESTCall(GITHUB_API_BASE_URL + GITHUB_API_REPO_Content_PATH + repoName +"/contents",
				"application/vnd.github.v3.text-match+json"));
 
		// To print response JSON, using GSON. Any other JSON parser can be used here.
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		System.out.println("<JSON RESPONSE START>\n" + gson.toJson(response) + "\n<JSON RESPONSE END>\n");
 
		// Iterate through list of file metadata from response.
		for (List<Map> fileMetaData : response) {
 
			// Get file name & raw file download URL from response.
			String fileName = (String) fileMetaData.get("name");
			String downloadUrl = (String) fileMetaData.get("download_url");
			System.out.println("File Name = " + fileName + " | Download URL = " + downloadUrl);
 
			// We will only fetch read me file for this example.
			if (downloadUrl != null && downloadUrl.contains("README")) {
 
				/*
				 * Get file content as string
				 * 
				 * Using Apache commons IO to read content from the remote URL. Any other HTTP
				 * client library can be used here.
				 */
				String fileContent = IOUtils.toString(new URI(downloadUrl), Charset.defaultCharset());
				System.out.println("\nfileContent = <FILE CONTENT START>\n" + fileContent + "\n<FILE CONTENT END>\n");
 
				/*
				 * Download read me file to local.
				 * 
				 * Using Apache commons IO to create file from remote content. Any other library
				 * or code can be written to get content from URL & create file in local.
				 */
				File file = new File("github-api-downloaded-" + fileName);
				FileUtils.copyURLToFile(new URL(downloadUrl), file);
			}
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
