package ch.rasc.iss;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.PostExchange;

import ch.rasc.iss.IssNotifyClient.Position;

public interface CentrifugoApiClient {

	record PublishPayload(String channel, Position data) {}
	
	@PostExchange("/publish")
	void publish(@RequestHeader("X-API-Key") String apiKey,
			@RequestBody PublishPayload payload);
	
}
