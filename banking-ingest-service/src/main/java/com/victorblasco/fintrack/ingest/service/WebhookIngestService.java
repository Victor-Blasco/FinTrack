package com.victorblasco.fintrack.ingest.service;

import com.victorblasco.fintrack.ingest.dto.RawTransactionEvent;
import com.victorblasco.fintrack.ingest.dto.TransactionWebhookPayload;
import com.victorblasco.fintrack.ingest.producer.RawTransactionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WebhookIngestService {

    private static final Logger log = LoggerFactory.getLogger(WebhookIngestService.class);

    private final RawTransactionProducer rawTransactionProducer;

    public WebhookIngestService(RawTransactionProducer rawTransactionProducer) {
        this.rawTransactionProducer = rawTransactionProducer;
    }

    public void processWebhook(TransactionWebhookPayload payload) {
        log.info("Procesando webhook PSD2 para transacción {}", payload.transactionId());

        RawTransactionEvent event = new RawTransactionEvent(
                payload.transactionId(),
                payload.accountNumber(),
                payload.amount(),
                payload.currency(),
                payload.merchant(),
                payload.timestamp(),
                "PSD2_WEBHOOK",
                null
        );

        rawTransactionProducer.send(event);
    }
}
