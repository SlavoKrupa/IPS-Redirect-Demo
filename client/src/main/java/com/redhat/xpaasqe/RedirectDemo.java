package com.redhat.xpaasqe;

import static java.util.Arrays.asList;

import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RedirectDemo {
   public static final String ENDPOINT = "http://ips-redirect-ips-redirect-demo.apps.latest.xpaas/kie-server/services/rest/server";
   public static final String USERNAME = "kieserver";
   public static final String PASSWORD = "kieserver1!";
   public static final String CONTAINER_ALIAS = "multipleVersionContainer";
   public static final String PROJECT_V1_GAV = "multipleVersionContainer=com.redhat.xpaasqe:UpdateDemo:1.0.0";
   public static final String PROJECT_V2_GAV = "multipleVersionContainer=com.redhat.xpaasqe:UpdateDemo:1.0.1";

   public static final String PROCESS_ID = "UpdateDemo.CaseSensitiveProcess";

   public static void main(String[] args) throws IOException {
      KieServicesConfiguration config = KieServicesFactory.newRestConfiguration(ENDPOINT, USERNAME, PASSWORD);

      KieServicesClient client = KieServicesFactory.newKieServicesClient(config);
      final ProcessServicesClient processServicesClient = client.getServicesClient(ProcessServicesClient.class);
      List<Long> oldProcessInstances = new ArrayList<>(4);
      oldProcessInstances.add(processServicesClient.startProcess(PROJECT_V1_GAV, PROCESS_ID));
      System.out.println("Conversation id example: " + client.getConversationId());
      System.out.println("Process with explicit V1 (GAV) container was started in: " +
              getContainerVersionFromConversationId(client.getConversationId()));
      for (int i = 0; i < 3; i++) {
         oldProcessInstances.add(processServicesClient.startProcess(CONTAINER_ALIAS, PROCESS_ID));
         System.out.println("Requests with conversation id V1 and  with container alias are redirected to: " + getContainerVersionFromConversationId(client.getConversationId()));
      }
      ProcessDefinition processDefinition = processServicesClient.getProcessDefinition(CONTAINER_ALIAS, PROCESS_ID);
      System.out.println("Process definition with conversation id is obtained in version: " + processDefinition.getVersion());
      List<Long> newProcessInstances = new ArrayList<>(4);
      newProcessInstances.add(processServicesClient.startProcess(PROJECT_V2_GAV, PROCESS_ID));
      System.out.println("Using GAV for explicit container V2 (GAV) results in calling the container:" + getContainerVersionFromConversationId(client.getConversationId()));
      client.completeConversation();
      newProcessInstances.add(processServicesClient.startProcess(CONTAINER_ALIAS, PROCESS_ID));
      System.out.println("Completing conversation and calling container alias should result in calling V2 container: " + getContainerVersionFromConversationId(client.getConversationId()));
      for (int i = 0; i < 2; i++) {
         newProcessInstances.add(processServicesClient.startProcess(CONTAINER_ALIAS, PROCESS_ID));
         System.out.println("Requests with conversation id V2 and  with container alias are redirected to: " + getContainerVersionFromConversationId(client.getConversationId()));
      }
      Iterator<Long> oldIterator = oldProcessInstances.iterator();
      Iterator<Long> newIterator = newProcessInstances.iterator();
      System.out.println("** Start calling single old");
      signalProcessInstanceAndObtainVariables(oldIterator.next(), client);
      System.out.println("** End calling single old");
      System.out.println("** Start calling single new");
      signalProcessInstanceAndObtainVariables(newIterator.next(), client);
      System.out.println("** End calling single new");
      System.out.println("** Start calling multiple old");
      signalProcessInstanceAndObtainVariables(asList(oldIterator.next(), oldIterator.next()), client);
      System.out.println("** End calling multiple old");
      System.out.println("** Start calling multiple mixed");
      signalProcessInstanceAndObtainVariables(asList(newIterator.next(), oldIterator.next()), client);
      System.out.println("** End calling multiple mixed");
      System.out.println("** Start calling multiple new");
      signalProcessInstanceAndObtainVariables(asList(newIterator.next(), newIterator.next()), client);
      System.out.println("** End calling multiple new");
   }

   private static void signalProcessInstanceAndObtainVariables(Long processInstanceId, KieServicesClient client) {
      final ProcessServicesClient processServicesClient = client.getServicesClient(ProcessServicesClient.class);
      final QueryServicesClient queryServicesClient = client.getServicesClient(QueryServicesClient.class);
      processServicesClient.signalProcessInstance(CONTAINER_ALIAS, processInstanceId, "variable", "Hello Process!");
      List<VariableInstance> variables = queryServicesClient.findVariablesCurrentState(processInstanceId);
      printVariables(variables);
   }

   private static void signalProcessInstanceAndObtainVariables(List<Long> processInstanceIds, KieServicesClient client) {
      client.completeConversation();
      final ProcessServicesClient processServicesClient = client.getServicesClient(ProcessServicesClient.class);
      final QueryServicesClient queryServicesClient = client.getServicesClient(QueryServicesClient.class);
      processServicesClient.signalProcessInstances(CONTAINER_ALIAS, processInstanceIds, "variable", "Hello Process!");
      processInstanceIds.forEach((processInstanceId) -> {
         List<VariableInstance> variables = queryServicesClient.findVariablesCurrentState(processInstanceId);
         printVariables(variables);
      });
   }

   private static void printVariables(List<VariableInstance> variables) {
      variables.forEach((variable) -> {
         System.out.println(String.format("Variable: %s with value: %s ", variable.getVariableName(), variable.getValue()));
      });
   }

   private static String getContainerVersionFromConversationId(String conversationId) {
      if (conversationId.contains("1.0.0")) {
         return "Container 1";
      } else if (conversationId.contains("1.0.1")) {
         return "Container 2";
      } else return "Unknown container";
   }
}
