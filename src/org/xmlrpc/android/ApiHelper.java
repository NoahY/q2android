package org.xmlrpc.android;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.xmlrpc.android.HttpRequest.HttpRequestException;

import android.content.Context;
import android.os.AsyncTask;
import android.text.format.DateUtils;

public class ApiHelper {
	/** Called when the activity is first created. */
	private static XMLRPCClient client;

	
	/**
	 * Discover the XML-RPC endpoint for the WordPress API associated with the specified blog URL.
	 *
	 * @param urlString URL of the blog to get the XML-RPC endpoint for.
	 * @return XML-RPC endpoint for the specified blog, or null if unable to discover endpoint.
	 */
	public static String getXMLRPCUrl(String urlString) {
		Pattern xmlrpcLink = Pattern.compile("<api\\s*?name=\"WordPress\".*?apiLink=\"(.*?)\"",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		String html = getResponse(urlString);
		if (html != null) {
			Matcher matcher = xmlrpcLink.matcher(html);
			if (matcher.find()) {
				String href = matcher.group(1);
				return href;
			}
		}
		return null; // never found the rsd tag
	}

	/**
	 * Discover the RSD homepage URL associated with the specified blog URL.
	 *
	 * @param urlString URL of the blog to get the link for.
	 * @return RSD homepage URL for the specified blog, or null if unable to discover URL.
	 */
	public static String getHomePageLink(String urlString) {
		Pattern xmlrpcLink = Pattern.compile("<homePageLink>(.*?)</homePageLink>",
				Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

		String html = getResponse(urlString);
		if (html != null) {
			Matcher matcher = xmlrpcLink.matcher(html);
			if (matcher.find()) {
				String href = matcher.group(1);
				return href;
			}
		}
		return null; // never found the rsd tag
	}

	/**
	 * Fetch the content stream of the resource at the specified URL.
	 *
	 * @param urlString URL to fetch contents for.
	 * @return content stream, or null if URL was invalid or resource could not be retrieved.
	 */
	public static InputStream getResponseStream(String urlString) {
		HttpRequest request = getHttpRequest(urlString);
		if (request != null) {
			return request.buffer();
		} else {
			return null;
		}
	}

	/**
	 * Fetch the content of the resource at the specified URL.
	 *
	 * @param urlString URL to fetch contents for.
	 * @return content of the resource, or null if URL was invalid or resource could not be retrieved.
	 */
	public static String getResponse(String urlString) {
		HttpRequest request = getHttpRequest(urlString);
		if (request != null) {
			return request.body();
		} else {
			return null;
		}
	}

	/**
	 * Fetch the specified HTTP resource.
	 *
	 * The URL class will automatically follow up to five redirects, with the
	 * exception of redirects between HTTP and HTTPS URLs. This method manually
	 * handles one additional redirect to allow for this protocol switch.
	 *
	 * @param urlString URL to fetch.
	 * @return the request / response object or null if the resource could not be retrieved.
	 */
	public static HttpRequest getHttpRequest(String urlString) {
		try {
			HttpRequest request = HttpRequest.get(urlString);

			// manually follow one additional redirect to support protocol switching
			if (request.code() == HttpURLConnection.HTTP_MOVED_PERM
					|| request.code() == HttpURLConnection.HTTP_MOVED_TEMP) {
				String location = request.location();
				if (location != null) {
					request = HttpRequest.get(location);
				}
			}

			return request;
		} catch (HttpRequestException e) {
			e.printStackTrace();
			return null;
		}
	}
}
