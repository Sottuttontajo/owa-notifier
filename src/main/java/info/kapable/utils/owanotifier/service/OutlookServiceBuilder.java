package info.kapable.utils.owanotifier.service;

import java.util.UUID;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;

public class OutlookServiceBuilder {

	public static OutlookService getOutlookService(String accessTokenSource, String userEmailSource) {
		// Create a request interceptor to add headers that belong on
		// every request
		final String userEmail = userEmailSource;
		final String accessToken = accessTokenSource;
		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addHeader("User-Agent", "java-tutorial");
				request.addHeader("client-request-id", UUID.randomUUID().toString());
				request.addHeader("return-client-request-id", "true");
				request.addHeader("Authorization", String.format("Bearer %s", accessToken));
				request.addHeader("Accept","application/json");

				if (userEmail != null && !userEmail.isEmpty()) {
					request.addHeader("X-AnchorMailbox", userEmail);
				}
			}
		};

		// Create and configure the Retrofit object
		RestAdapter restAdapter = new RestAdapter.Builder()
				// Retrofit retrofit = new Retrofit.Builder()
				.setEndpoint("https://graph.microsoft.com")
				/*
				 * .client(client)
				 * .addConverterFactory(JacksonConverterFactory.create())
				 */
				.setRequestInterceptor(requestInterceptor).setLogLevel(LogLevel.FULL).setLog(new RestAdapter.Log() {
					@Override
					public void log(String msg) {
						System.out.println(msg);
					}
				}).build();

		// Generate the token service
		return restAdapter.create(OutlookService.class);
	}
}