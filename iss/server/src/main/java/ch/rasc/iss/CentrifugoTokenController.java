package ch.rasc.iss;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.UUID;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@RestController
public class CentrifugoTokenController {

	private Key centrifugoHmacShaKey;
	
	public CentrifugoTokenController(CentrifugoConfig centrifugoConfig) {		
		this.centrifugoHmacShaKey = Keys
				.hmacShaKeyFor(centrifugoConfig.hmacSecret().getBytes(StandardCharsets.UTF_8));
	}
	
	@CrossOrigin(origins =  {"${settings.cors_origin}"})
	@GetMapping("/centrifugo-token")
	@ResponseBody
	public String token() {
		return Jwts.builder()
			.setSubject(UUID.randomUUID().toString())
			.signWith(this.centrifugoHmacShaKey, SignatureAlgorithm.HS256)
			.compact();
	}
}
