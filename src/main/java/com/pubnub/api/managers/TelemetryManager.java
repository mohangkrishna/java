package com.pubnub.api.managers;

import com.pubnub.api.enums.PNOperationType;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class TelemetryManager {

    /**
     * Timer for telemetry information clean up.
     */
    private Timer timer;

    private Map<String, List<Map<String, Double>>> latencies;

    private static final int TIMESTAMP_DIVIDER = 1000;

    private static final double MAXIMUM_LATENCY_DATA_AGE = 60.0f;
    private static final int CLEAN_UP_INTERVAL = 1;
    private static final int CLEAN_UP_INTERVAL_MULTIPLIER = 1000;

    public TelemetryManager() {

        this.latencies = new HashMap<>();
        startCleanUpTimer();
    }


    public synchronized Map<String, String> operationsLatency() {
        Map<String, String> operationLatencies = new HashMap<>();
        for (Map.Entry<String, List<Map<String, Double>>> entry : this.latencies.entrySet()) {
            String latencyKey = "l_".concat(entry.getKey());
            double endpointAverageLatency = TelemetryManager.averageLatencyFromData(entry.getValue());
            if (endpointAverageLatency > 0.0f) {
                operationLatencies.put(latencyKey, Double.toString(endpointAverageLatency));
            }
        }
        return operationLatencies;
    }

    public synchronized void storeLatency(long latency, PNOperationType type) {
        if (type != PNOperationType.PNSubscribeOperation && latency > 0) {
            String endpointName = TelemetryManager.endpointNameForOperation(type);
            if (endpointName != null) {
                double storeDate = (new Date()).getTime() / (double) TIMESTAMP_DIVIDER;

                List<Map<String, Double>> operationLatencies = this.latencies.get(endpointName);
                if (operationLatencies == null) {
                    operationLatencies = new ArrayList<>();
                    this.latencies.put(endpointName, operationLatencies);
                }

                Map<String, Double> latencyEntry = new HashMap<>();
                latencyEntry.put("d", storeDate);
                latencyEntry.put("l", ((double) latency / TIMESTAMP_DIVIDER));
                operationLatencies.add(latencyEntry);
            }
        }
    }

    private synchronized void cleanUpTelemetryData() {
        double currentDate = (new Date()).getTime() / (double) TIMESTAMP_DIVIDER;
        List<String> endpoints = new ArrayList<>(this.latencies.keySet());
        for (String endpoint : endpoints) {
            List<Map<String, Double>> outdatedLatencies = new ArrayList<>();
            List<Map<String, Double>> operationLatencies = this.latencies.get(endpoint);
            for (Map<String, Double> latencyInformation : operationLatencies) {
                if (currentDate - latencyInformation.get("d") > MAXIMUM_LATENCY_DATA_AGE) {
                    outdatedLatencies.add(latencyInformation);
                }
            }
            if (outdatedLatencies.size() > 0) {
                operationLatencies.removeAll(outdatedLatencies);
            }
            if (operationLatencies.size() == 0) {
                this.latencies.remove(endpoint);
            }
        }
    }

    private void startCleanUpTimer() {
        long interval = CLEAN_UP_INTERVAL * CLEAN_UP_INTERVAL_MULTIPLIER;

        stopCleanUpTimer();
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                cleanUpTelemetryData();
            }
        }, interval, interval);
    }

    public void stopCleanUpTimer() {
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    private static double averageLatencyFromData(List<Map<String, Double>> endpointLatencies) {
        double totalLatency = 0.0f;
        for (Map<String, Double> item : endpointLatencies) {
            totalLatency += item.get("l");
        }

        return totalLatency / endpointLatencies.size();
    }

    private static String endpointNameForOperation(PNOperationType type) {
        String endpoint;
        switch (type) {
            case PNPublishOperation:
                endpoint = "pub";
                break;
            case PNHistoryOperation:
            case PNFetchMessagesOperation:
            case PNDeleteMessagesOperation:
                endpoint = "hist";
                break;
            case PNUnsubscribeOperation:
            case PNWhereNowOperation:
            case PNHereNowOperation:
            case PNHeartbeatOperation:
            case PNSetStateOperation:
            case PNGetState:
                endpoint = "pres";
                break;
            case PNAddChannelsToGroupOperation:
            case PNRemoveChannelsFromGroupOperation:
            case PNChannelGroupsOperation:
            case PNRemoveGroupOperation:
            case PNChannelsForGroupOperation:
                endpoint = "cg";
                break;
            case PNPushNotificationEnabledChannelsOperation:
            case PNAddPushNotificationsOnChannelsOperation:
            case PNRemovePushNotificationsFromChannelsOperation:
            case PNRemoveAllPushNotificationsOperation:
                endpoint = "push";
                break;
            case PNAccessManagerAudit:
            case PNAccessManagerGrant:
                endpoint = "pam";
                break;
            case PNMessageCountOperation:
                endpoint = "mc";
                break;
            case PNSignalOperation:
                endpoint = "sig";
                break;
            case PNCreateUserOperation:
            case PNGetUserOperation:
            case PNGetUsersOperation:
            case PNUpdateUserOperation:
            case PNDeleteUserOperation:
                endpoint = "obj";
                break;
            case PNCreateSpaceOperation:
            case PNGetSpaceOperation:
            case PNGetSpacesOperation:
            case PNUpdateSpaceOperation:
            case PNDeleteSpaceOperation:
                endpoint = "obj";
                break;
            case PNGetMembers:
            case PNManageMembers:
                endpoint = "obj";
                break;
            case PNGetMemberships:
            case PNManageMemberships:
                endpoint = "obj";
                break;
            default:
                endpoint = "time";
                break;
        }

        return endpoint;
    }
}
