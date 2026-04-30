import io.grpc.Server;
import io.grpc.ServerBuilder;
import service.ReferenceServiceImpl;

import java.io.IOException;
import java.util.logging.Logger;

public class ReferenceApplication {
    private static final Logger logger = Logger.getLogger(ReferenceApplication.class.getName());
    private static final int PORT = 50051;
    private Server server;

    public void start() throws IOException {
        server = ServerBuilder.forPort(PORT)
                .addService(new ReferenceServiceImpl())
                .build()
                .start();

        logger.info("Reference Service B запущен на порту " + PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Завершение Reference Service...");
            if (server != null) {
                server.shutdown();
            }
        }));
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ReferenceApplication app = new ReferenceApplication();
        app.start();
        app.blockUntilShutdown();
    }
}