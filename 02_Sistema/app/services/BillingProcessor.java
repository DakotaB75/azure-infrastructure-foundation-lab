import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BillingProcessor {

    private static final Logger LOGGER =
            Logger.getLogger(BillingProcessor.class.getName());

    private static final String ENVIRONMENT = "PRODUCTION";,
    private static final String REGION = "South America East";

    private BillingProcessor() {
    }

    public static void main(String[] args) {

        LOGGER.info("Initializing billing platform...");

        BillingContext context = BillingContext.initialize();

        BillingRequest request = new BillingRequest(
                UUID.randomUUID().toString(),
                "CONTOSO-CLIENT-001",
                24999.90,
                Currency.ARS
        );

        BillingResult result = processBilling(request, context);

        printExecutionSummary(result);
    }

    public static BillingResult processBilling(
            BillingRequest request,
            BillingContext context
    ) {

        LOGGER.info("Starting billing transaction validation.");

        validateRequest(request);

        LOGGER.info("Validation completed successfully.");

        String transactionId = generateTransactionId();

        LOGGER.info(() ->
                String.format(
                        "Transaction %s created for customer %s",
                        transactionId,
                        request.customerId()
                )
        );

        double taxAmount = calculateTax(request.amount(), 0.21);
        double totalAmount = request.amount() + taxAmount;

        LOGGER.info(() ->
                String.format(
                        "Billing calculation completed. Total amount: %.2f %s",
                        totalAmount,
                        request.currency()
                )
        );

        return new BillingResult(
                transactionId,
                request.customerId(),
                request.amount(),
                taxAmount,
                totalAmount,
                BillingStatus.COMPLETED,
                LocalDateTime.now(),
                context.environment()
        );
    }

    private static void validateRequest(BillingRequest request) {

        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new IllegalArgumentException(
                    "Customer identifier cannot be null or empty."
            );
        }

        if (request.amount() <= 0) {
            throw new IllegalArgumentException(
                    "Billing amount must be greater than zero."
            );
        }
    }

    private static double calculateTax(double amount, double taxRate) {
        return amount * taxRate;
    }

    private static String generateTransactionId() {
        return "TXN-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }

    private static void printExecutionSummary(BillingResult result) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        System.out.println("\n========================================");
        System.out.println(" ENTERPRISE BILLING EXECUTION SUMMARY ");
        System.out.println("========================================");
        System.out.println("Transaction ID : " + result.transactionId());
        System.out.println("Customer ID    : " + result.customerId());
        System.out.println("Subtotal       : " + result.subtotal());
        System.out.println("Tax Amount     : " + result.taxAmount());
        System.out.println("Total Amount   : " + result.totalAmount());
        System.out.println("Status         : " + result.status());
        System.out.println("Environment    : " + result.environment());
        System.out.println("Processed At   : "
                + result.processedAt().format(formatter));
        System.out.println("========================================");
    }
    record BillingContext(String environment, String region) {

        static BillingContext initialize() {

            LOGGER.log(
                    Level.INFO,
                    "Environment: {0} | Region: {1}",
                    new Object[]{ENVIRONMENT, REGION}
            );

            return new BillingContext(ENVIRONMENT, REGION);
        }
    }

    record BillingRequest(
            String requestId,
            String customerId,
            double amount,
            Currency currency
    ) {
    }
    record BillingResult(
            String transactionId,
            String customerId,
            double subtotal,
            double taxAmount,
            double totalAmount,
            BillingStatus status,
            LocalDateTime processedAt,
            String environment
    ) {
    }
    enum Currency {
        USD,
        EUR,
        ARS
    }
    enum BillingStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
}
