package info.kapable.utils.owanotifier;

import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.kapable.utils.owanotifier.auth.AuthHelper;
import info.kapable.utils.owanotifier.auth.IdToken;
import info.kapable.utils.owanotifier.auth.TokenResponse;
import info.kapable.utils.owanotifier.resource.Labels;
import info.kapable.utils.owanotifier.service.OutlookService;
import info.kapable.utils.owanotifier.service.OutlookServiceBuilder;

public class TokenResponseValidator
{
	private static Logger logger = LoggerFactory.getLogger(TokenResponseValidator.class);

	// A public object to store auth
	public TokenResponse tokenResponse;
	private IdToken idToken;
	private OutlookService outlookService;

	public boolean validate() throws IOException
	{
		if(outlookService == null)
			outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), null);

		if(isTokenExpired())
		{
			logger.info(Labels.getLabel("token.refresh"));
			TokenResponse tokenResponse = refreshToken();
			if(isTokenRefreshed(tokenResponse))
			{
				logger.info(Labels.getLabel("token.refreshed"));
				outlookService = OutlookServiceBuilder.getOutlookService(tokenResponse.getAccessToken(), null);
				setTokenResponse(tokenResponse);
				return true;
			}
			else
			{
				logger.info(Labels.getLabel("token.not_refreshed"));
				return false;
			}
		}
		else
			return true;
	}

	private boolean isTokenExpired()
	{
		Calendar now = Calendar.getInstance();
		return tokenResponse.getExpirationTime().before(now.getTime());
	}

	private TokenResponse refreshToken()
	{
		return AuthHelper.getTokenFromRefresh(this.tokenResponse, idToken.getTenantId());
	}

	public TokenResponse getTokenResponse()
	{
		return tokenResponse;
	}

	public void setTokenResponse(TokenResponse tokenResponse)
	{
		this.tokenResponse = tokenResponse;
	}

	private boolean isTokenRefreshed(TokenResponse tokenResponse)
	{
		return tokenResponse.getError() == null;
	}

	public IdToken getIdToken()
	{
		return idToken;
	}

	public void setIdToken(IdToken idToken)
	{
		this.idToken = idToken;
	}
	
	public OutlookService getOutlookService()
	{
		return outlookService;
	}
}
