package com.dibimbing.inventory_sales_api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AuditLogger {
    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    public static void log(String action, String entityType, Long entityId, String actorEmail, Map<String, Object> meta) {
        AUDIT.info("action={} entityType={} entityId={} actor={} meta={}",
                action, entityType, entityId, actorEmail, meta);
    }
}