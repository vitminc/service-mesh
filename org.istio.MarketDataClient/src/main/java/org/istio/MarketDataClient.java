package org.istio;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.istio.marketdata.MarketDataGrpc;
import org.istio.marketdata.Security;
import org.istio.marketdata.SecurityCodeType;
import org.istio.marketdata.SecurityInfo;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MarketDataClient {

    private static final Logger logger = Logger.getLogger(MarketDataClient.class.getName());

    private final ManagedChannel channel;
    private final MarketDataGrpc.MarketDataBlockingStub blockingStub;

    static {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    private static SslContext buildSslContext(String trustCertCollectionFilePath,
                                              String clientCertChainFilePath,
                                              String clientPrivateKeyFilePath) throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        if (trustCertCollectionFilePath != null) {
            builder.trustManager(new File(trustCertCollectionFilePath));
        }

        if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
            builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
        }

        return builder.build();
    }

    /**
     * Construct client connecting to the server at {@code host:port}.
     */
    public MarketDataClient(String host,
                            int port,
                            SslContext sslContext,
                            JWTClientInterceptor clientInterceptor) throws SSLException {

        this(NettyChannelBuilder.forAddress(host, port)
                .negotiationType(NegotiationType.TLS)
                .sslContext(sslContext)
                .intercept(clientInterceptor)
                .build());
    }

    /**
     * Construct client connecting to the server at {@code host:port}.
     */
    public MarketDataClient(String host,
                            int port,
                            JWTClientInterceptor clientInterceptor) throws SSLException {

        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(clientInterceptor)
                .build());
    }

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    MarketDataClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = MarketDataGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Update inventory upon confirming an order
     */
    public void updateSecurity(String securityCode) {

        Security.Builder builder = Security.newBuilder()
                .setSecurityCode(securityCode)
                .setSecurityCodeType(securityCode.length() == 6 ? SecurityCodeType.WKN : SecurityCodeType.ISIN);

        SecurityInfo updateResponse;
        try {
            updateResponse = blockingStub.updateSecurity(builder.build());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }

        logger.info("Security name: " + updateResponse.getSecurityName());
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting.
     */
    public static void main(String[] args) throws Exception {

        if (args.length > 0 && (args.length < 2 || args.length == 4 || args.length > 5)) {
            System.out.println("USAGE: InventoryClient host port [trustCertCollectionFilePath] " +
                    "[clientCertChainFilePath clientPrivateKeyFilePath]\n  Note: clientCertChainFilePath and " +
                    "clientPrivateKeyFilePath are only needed if mutual auth is desired.");
            System.exit(0);
        }

        String hostname = System.getenv("INVENTORY_HOST");
        if (hostname == null || hostname.isEmpty()) {
            hostname = "localhost";
        }

        if (args.length > 1 && args[0] != null && !args[0].isEmpty()) {
            hostname = args[0].trim();
        }

        String port = System.getenv("INVENTORY_PORT");
        if (port == null || port.isEmpty()) {
            port = "50051";
        }

        if (args.length > 1 && args[1] != null && !args[1].isEmpty()) {
            port = args[1].trim();
        }

        System.out.println("Hostname: " + hostname);
        System.out.println("Port: " + port);

        JWTClientInterceptor clientInterceptor = new JWTClientInterceptor();
        clientInterceptor.setTokenValue(System.getenv("TOKEN"));

        MarketDataClient client;
        switch (args.length) {
            case 0:
                client = new MarketDataClient(hostname, Integer.parseInt(port), clientInterceptor);
                break;
            case 2:
                client = new MarketDataClient(hostname, Integer.parseInt(port), clientInterceptor);
                break;
            case 3:
                client = new MarketDataClient(hostname, Integer.parseInt(port),
                        buildSslContext(args[2], null, null), clientInterceptor);
                break;
            default:
                client = new MarketDataClient(hostname, Integer.parseInt(port),
                        buildSslContext(args[2], args[3], args[4]), clientInterceptor);
        }

        try {
            client.updateSecurity("710000");
        } finally {
            client.shutdown();
        }
    }
}
