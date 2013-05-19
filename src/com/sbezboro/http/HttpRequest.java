package com.sbezboro.http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Hashtable;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.sbezboro.http.listeners.HttpRequestListener;

public abstract class HttpRequest {
	protected enum HTTPMethod {
		GET,
		POST
	};

	protected final Plugin plugin;
	private Hashtable<String, String> properties;
	private HTTPMethod method;
	
	public HttpRequest(Plugin plugin, HTTPMethod method) {
		this.plugin = plugin;
		this.method = method;
		properties = new Hashtable<String, String>();
	}
	
	public void addProperty(String key, String value) {
		properties.put(key, value);
	}
	
	public void addProperty(String key, int value) {
		properties.put(key, String.valueOf(value));
	}
	
	public void addProperty(String key, boolean value) {
		properties.put(key, value ? "1" : "0");
	}
	
	private String getPropertyData() {
		String data = "";
		
		int i = 0;
		for (String key : properties.keySet()) {
			String value = properties.get(key);
			
			try {
				data += URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8");
				if (i < properties.keySet().size() - 1) {
					data += "&";
				}
			} catch (UnsupportedEncodingException e) {
			}
			
			i++;
		}
		
		return data;
	}
	
	public void start() {
		start(null);
	}
	
	public void start(final HttpRequestListener listener) {
		new Thread() {
			@Override
			public void run() {
				super.run();
				
				try {
					String urlString = getUrl();
					if (method == HTTPMethod.GET) {
						urlString += "?" + getPropertyData();
					}
					
					URL url = new URL(urlString);
			        URLConnection conn = url.openConnection();
			        
			        if (method == HTTPMethod.POST) { 
				        conn.setDoOutput(true);
				        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
				        wr.write(getPropertyData());
				        wr.flush();
				        wr.close();
			        }

			        String response = "";
			        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			        String line;
			        while ((line = rd.readLine()) != null) {
			        	response += line;
			        }
			        rd.close();

			        HttpResponse httpResponse;
			        JSONObject jsonResponse = null;
			        try {
			        	jsonResponse = (JSONObject) JSONValue.parse(response);
			        } catch (Exception e) {
			        }
			        
			        if (jsonResponse == null) {
			        	httpResponse = new HttpResponse(response);
			        } else {
			        	httpResponse = new HttpResponse(jsonResponse);
			        }
			        
			        if (listener != null) {
				        final HttpResponse finalResponse = httpResponse;
				        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
						        listener.requestSuccess(finalResponse);
							}
						});
			        }
				} catch (final Exception e) {
			        if (listener != null) {
				        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
							
							@Override
							public void run() {
								listener.requestFailure(new HttpResponse(e.toString()));
							}
						});
			        }
				}
			}
		}.start();
	}
	
	public abstract String getUrl();
}