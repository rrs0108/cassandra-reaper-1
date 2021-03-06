package com.spotify.reaper;

import com.google.common.base.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.reaper.resources.view.ClusterStatus;
import com.spotify.reaper.resources.view.RepairRunStatus;
import com.spotify.reaper.resources.view.RepairScheduleStatus;

import org.glassfish.jersey.client.ClientResponse;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

/**
 * This is a simple client for testing usage, that calls the Reaper REST API
 * and turns the resulting JSON into Reaper core entity instances.
 */
public class SimpleReaperClient {

  private static final Logger LOG = LoggerFactory.getLogger(SimpleReaperClient.class);

  private static Optional<Map<String, String>> EMPTY_PARAMS = Optional.absent();

  public static Response doHttpCall(String httpMethod, String host, int port, String urlPath,
                                          Optional<Map<String, String>> params) {
    String reaperBase = "http://" + host.toLowerCase() + ":" + port + "/";
    URI uri;
    try {
      uri = new URL(new URL(reaperBase), urlPath).toURI();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    
    Client client = ClientBuilder.newClient();
    WebTarget webTarget = client.target(uri);
    
    
    LOG.info("calling (" + httpMethod + ") Reaper in resource: " + webTarget.getUri());
    if (params.isPresent()) {
      for (Map.Entry<String, String> entry : params.get().entrySet()) {
        webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
      }
    }
    
    Invocation.Builder invocationBuilder =
        webTarget.request(MediaType.APPLICATION_JSON);
    
    Response response;
    if ("GET".equalsIgnoreCase(httpMethod)) {
      response = invocationBuilder.get();
    } else if ("POST".equalsIgnoreCase(httpMethod)) {
      response = invocationBuilder.post(null);
    } else if ("PUT".equalsIgnoreCase(httpMethod)) {
      response = invocationBuilder.put(Entity.entity("",MediaType.APPLICATION_JSON));
    } else if ("DELETE".equalsIgnoreCase(httpMethod)) {
      response = invocationBuilder.delete();
    } else if ("OPTIONS".equalsIgnoreCase(httpMethod)) {
      response = invocationBuilder.options();
    } else {
      throw new RuntimeException("Invalid HTTP method: " + httpMethod);
    }

    return response; 
  }

  private static <T> T parseJSON(String json, TypeReference<T> ref) {
    T parsed;
    ObjectMapper mapper = new ObjectMapper();
    try {
      parsed = mapper.readValue(json, ref);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
    return parsed;
  }

  public static List<RepairScheduleStatus> parseRepairScheduleStatusListJSON(String json) {
    return parseJSON(json, new TypeReference<List<RepairScheduleStatus>>() {
    });
  }

  public static RepairScheduleStatus parseRepairScheduleStatusJSON(String json) {
    return parseJSON(json, new TypeReference<RepairScheduleStatus>() {
    });
  }

  public static List<RepairRunStatus> parseRepairRunStatusListJSON(String json) {
    return parseJSON(json, new TypeReference<List<RepairRunStatus>>() {
    });
  }

  public static RepairRunStatus parseRepairRunStatusJSON(String json) {
    return parseJSON(json, new TypeReference<RepairRunStatus>() {
    });
  }
  
  public static Map<String, Object> parseClusterStatusJSON(String json) {
    return parseJSON(json, new TypeReference<Map<String, Object> >() {
    });
  }
  
  public static List<String> parseClusterNameListJSON(String json) {
    return parseJSON(json, new TypeReference<List<String> >() {
    });
  }

  private String reaperHost;
  private int reaperPort;

  public SimpleReaperClient(String reaperHost, int reaperPort) {
    this.reaperHost = reaperHost;
    this.reaperPort = reaperPort;
  }

  public List<RepairScheduleStatus> getRepairSchedulesForCluster(String clusterName) {
    Response response = doHttpCall("GET", reaperHost, reaperPort,
                                         "/repair_schedule/cluster/" + clusterName, EMPTY_PARAMS);
    assertEquals(200, response.getStatus());
    String responseData = response.readEntity(String.class);
    return parseRepairScheduleStatusListJSON(responseData); 
  }

}
