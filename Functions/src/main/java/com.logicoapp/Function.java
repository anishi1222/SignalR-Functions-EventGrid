package com.logicoapp;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.signalr.SignalRConnectionInfo;
import com.microsoft.azure.functions.signalr.SignalRMessage;
import com.microsoft.azure.functions.signalr.annotation.SignalRConnectionInfoInput;
import com.microsoft.azure.functions.signalr.annotation.SignalROutput;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Optional;

public class Function {

    @FunctionName("negotiate")
    public SignalRConnectionInfo negotiate(
            @HttpTrigger(name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
            @SignalRConnectionInfoInput(name = "connectionInfo",
                    hubName = "testhub") SignalRConnectionInfo connectionInfo) {
        return connectionInfo;
    }

    @FunctionName("eventGridMonitor")
    public void logEvent(
            @EventGridTrigger(name = "event") String event,
            final ExecutionContext context) {
        // log
        context.getLogger().info("Event content: " + event);
    }

    @SignalROutput(name = "$return", hubName = "testhub")
    public SignalRMessage sendClimateInfo(
            @CosmosDBTrigger(feedPollDelay = 1000,
                    name = "climateBinding",
                    databaseName = "ClimateDB",
                    collectionName = "ClimateContainer",
                    leaseCollectionName = "Leases",
                    createLeaseCollectionIfNotExists = true,
                    connectionStringSetting = "AzureCosmosDBConnection")
                    ArrayList<String> climateJSONList,
            final ExecutionContext context) {
        context.getLogger().info(climateJSONList.size() + " record(s) is/are inserted.");
        climateJSONList.forEach(s -> System.err.println("Changed: " + s));
        ArrayList<ClimateInfo> climateInfoArrayList = new ArrayList<>();
        for (String s : climateJSONList) {
            JSONObject json = new JSONObject(s);
            climateInfoArrayList.add(new ClimateInfo(json.getString("device"), json.getString("timestamp"), json.getFloat("humid"), json.getFloat("temp")));
        }
        SignalRMessage message = new SignalRMessage();
        message.target = "climates";
        message.arguments.addAll(climateInfoArrayList);
        return message;
    }
}
