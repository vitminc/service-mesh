package org.istio.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.istio.marketdata.Security;
import org.istio.marketdata.SecurityInfo;

import java.io.IOException;
import java.util.logging.Logger;

public class MarketDataServer {

    private static final Logger logger = Logger.getLogger(MarketDataServer.class.getName());

    private Server server;

    public static void main(String[] args) throws InterruptedException, IOException {
        final MarketDataServer server = new MarketDataServer();
        server.start();
        server.blockUntilShutdown();
    }

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = ServerBuilder.forPort(port)
                .addService(new MarketDataImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                MarketDataServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {

        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {

        if (server != null) {
            server.awaitTermination();
        }
    }

    static class MarketDataImpl extends org.istio.marketdata.MarketDataGrpc.MarketDataImplBase {
        @Override
        public void updateSecurity(Security request, StreamObserver<SecurityInfo> responseObserver) {
            SecurityInfo.Builder builder = SecurityInfo.newBuilder()
                    .setSecurityCode(request.getSecurityCode())
                    .setSecurityCodeType(request.getSecurityCodeType())
                    .setSecurityName(request.getSecurityCode() + " Name");
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        }
    }
}
