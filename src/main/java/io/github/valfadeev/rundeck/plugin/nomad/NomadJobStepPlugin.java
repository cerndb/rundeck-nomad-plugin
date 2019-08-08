package io.github.valfadeev.rundeck.plugin.nomad;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dtolabs.rundeck.core.common.INodeEntry;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.execution.workflow.steps.StepException;
import com.dtolabs.rundeck.core.execution.workflow.steps.node.NodeStepException;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.step.StepPlugin;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.dtolabs.rundeck.plugins.util.PropertyBuilder;
import com.hashicorp.nomad.apimodel.JobListStub;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.Node;
import com.hashicorp.nomad.javasdk.JobsApi;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.ServerQueryResponse;
import com.dtolabs.rundeck.plugins.descriptions.PluginDescription;

import io.github.valfadeev.rundeck.plugin.nomad.NomadNodeStepPlugin.Reason;
import io.github.valfadeev.rundeck.plugin.nomad.common.Driver;
import io.github.valfadeev.rundeck.plugin.nomad.common.SupportedDrivers;
import io.github.valfadeev.rundeck.plugin.nomad.util.ParseInput;

@Driver(name = SupportedDrivers.DOCKER)
@Plugin(name = NomadJobStepPlugin.SERVICE_PROVIDER_NAME,
        service = ServiceNameConstants.WorkflowStep)
@PluginDescription(title = "Perform housekeeping operations on a Job",
                   description = "Perform Nomad housekeeping operations on a Job")

public class NomadJobStepPlugin implements StepPlugin, Describable {

    public static final String SERVICE_PROVIDER_NAME
            = "io.github.valfadeev.rundeck.plugin.nomad.NomadJobStepPlugin";
    
    public Description getDescription() {
        return DescriptionBuilder.builder()
            .name(SERVICE_PROVIDER_NAME)
            .title("NomadJobStep")
            .description("Nomad Job Workflow Step")
            .property(PropertyBuilder.builder()
                          .freeSelect("taskSelect")
                          .title("Job Operation")
                          .description("Job Operation")
                          .required(true)
                          .defaultValue("Stop")
                          .values("Start", "Stop", "Update", "Purge")
                          .build()
            )
            .property(PropertyBuilder.builder()
                          .string("datacenter")
                          .title("Datacenter")
                          .description("New Datacenter value")
                          .required(false)
                          .build()
            )
            .build();
   }
    
   public void executeStep(final PluginStepContext context, final Map<String, Object> configuration) throws StepException {
        
        PluginLogger logger = context.getExecutionContext().getExecutionListener();
        logger.log(2, "Configuration: " + configuration);
        
        INodeSet Nodes = context.getNodes();
        logger.log(5, Nodes.toString());
        for (INodeEntry node : Nodes.getNodes()) {

                logger.log(2, "Node: " + node.getNodename());
                logger.log(2, node.toString());

                Map<String, String> attributes = node.getAttributes();

                String nomadTokenStoragePath = attributes.get("token_path");
                logger.log(5, "Accesing storage path " + nomadTokenStoragePath);
                String nomadAuthToken = ParseInput.getPWFromStorage(context, nomadTokenStoragePath);

                NomadApiConfiguration config =
                        new NomadApiConfiguration
                                .Builder()
                                .setAddress(node.getAttributes().get("nomad_url"))
                                .setAuthToken(nomadAuthToken)
                                .build();
                NomadApiClient apiClient = new NomadApiClient(config);

                JobsApi jobsApi = apiClient.getJobsApi();

                ServerQueryResponse<List<JobListStub>> jobs;
                try {
                        jobs = jobsApi.list(node.getNodename());
                        for (JobListStub job : jobs.getValue()) {
                                logger.log(5, "Nomad Job: " + job.toString());
                                switch(configuration.get("taskSelect").toString()) {
                                        case "Start":
                                                logger.log(2, "Starting job");
                                                Job j = new Job();
                                                jobsApi.register(j);
                                                break;
                                        case "Stop": 
                                                logger.log(2, "Stopping job");
                                                jobsApi.deregister(job.getId());
                                                break;
                                        case "Update": 
                                                logger.log(2, "Updating job");
                                                break;
                                        case "Purge": 
                                                logger.log(2, "Purging job");
                                                jobsApi.deregister(job.getId(), true);
                                                break;
                                        default:logger.log(2, "Task not implemented");
                                                break;

                                }
                        }
                } catch (IOException | NomadException e) {
                        logger.log(0, "Nomad API Error:" + e.getMessage());
                        e.printStackTrace();
                }
        }


   }
}
