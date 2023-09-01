package ch.rasc.iss;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import ch.rasc.iss.CentrifugoApiClient.PublishPayload;
import ch.rasc.iss.IssNotifyClient.IssNotify;

@Service
public class IssPositionService {

	private final IssNotifyClient issNotifyClient;

	private final CentrifugoApiClient centrifugoApiClient;
	
	private final CentrifugoConfig centrifugoConfig;
	
	public IssPositionService(CentrifugoConfig centrifugoConfig) {
		this.centrifugoConfig = centrifugoConfig;
		
		WebClient issNotifyWebClient = WebClient.builder().baseUrl(IssNotifyClient.ISS_NOTIFY_URL)
				.build();
		HttpServiceProxyFactory issNotifyClientFactory = HttpServiceProxyFactory
				.builder(WebClientAdapter.forClient(issNotifyWebClient)).build();
		this.issNotifyClient = issNotifyClientFactory.createClient(IssNotifyClient.class);

		WebClient centrifugoApiWebClient = WebClient.builder().baseUrl(centrifugoConfig.apiBaseUrl())
				.build();
		HttpServiceProxyFactory centrifugoApiCientFactory = HttpServiceProxyFactory
				.builder(WebClientAdapter.forClient(centrifugoApiWebClient)).build();
		this.centrifugoApiClient = centrifugoApiCientFactory.createClient(CentrifugoApiClient.class);		
	}
	
	@Scheduled(initialDelay = 1000, fixedDelay = 3000)
	public void publish() {
		IssNotify currentLocation = this.issNotifyClient.issNow();
		if (currentLocation.message().equals("success")) {
			this.centrifugoApiClient.publish(this.centrifugoConfig.apiKey(),
					new PublishPayload("iss", currentLocation.position()));
		}		
	}
}
