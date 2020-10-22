package org.istio.marketdatarest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.istio.JWTClientInterceptor;
import org.istio.marketdata.MarketDataGrpc;
import org.istio.marketdata.Security;
import org.istio.marketdata.SecurityCodeType;
import org.istio.marketdata.SecurityInfo;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
@Controller
public class MarketDataRestApplication implements ApplicationRunner {

	private static final Logger logger = Logger.getLogger(MarketDataRestApplication.class.getName());

	private ManagedChannel channel;
	private MarketDataGrpc.MarketDataBlockingStub blockingStub;

	public static void main(String[] args) {
		SpringApplication.run(MarketDataRestApplication.class, args);
	}

	@RequestMapping(value = "/security/{security}", method = RequestMethod.GET)
	public ResponseEntity<?> checkOrderStatus(@PathVariable("security") String security) {
		SecurityInfo securityInfo = updateSecurity(security);
		if (securityInfo==null) {
			return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build();
		} else {
			return ResponseEntity.ok(
					"{'security' : '"+securityInfo.getSecurityCode()+"'," +
					"'type' : '"+securityInfo.getSecurityCodeType()+"',"+
					"'name' : '"+securityInfo.getSecurityName()+"'}");
		}
	}

	/**
	 * Update inventory upon confirming an order
     */
	public SecurityInfo updateSecurity(String securityCode) {

		Security.Builder builder = Security.newBuilder()
				.setSecurityCode(securityCode)
				.setSecurityCodeType(securityCode.length() == 6 ? SecurityCodeType.WKN : SecurityCodeType.ISIN);

		SecurityInfo updateResponse;
		try {
			return blockingStub.updateSecurity(builder.build());
		} catch (StatusRuntimeException e) {
			logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
			return null;
		}
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String hostname = "localhost";
		int port = 50051;

		JWTClientInterceptor clientInterceptor = new JWTClientInterceptor();
		clientInterceptor.setTokenValue(System.getenv("TOKEN"));

		this.channel = ManagedChannelBuilder.forAddress(hostname, port)
				.usePlaintext()
				.intercept(clientInterceptor)
				.build();
		this.blockingStub =  MarketDataGrpc.newBlockingStub(channel);
	}
}
