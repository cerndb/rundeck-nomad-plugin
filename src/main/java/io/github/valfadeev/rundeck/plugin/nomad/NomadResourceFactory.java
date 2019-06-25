package io.github.valfadeev.rundeck.plugin.nomad;

import com.dtolabs.rundeck.core.common.Framework;
import com.dtolabs.rundeck.core.common.INodeSet;
import com.dtolabs.rundeck.core.common.NodeEntryImpl;
import com.dtolabs.rundeck.core.common.NodeSetImpl;
import com.dtolabs.rundeck.core.data.DataContext;
import com.dtolabs.rundeck.core.execution.ExecutionContext;
import com.dtolabs.rundeck.core.execution.workflow.FlowControl;
import com.dtolabs.rundeck.core.execution.workflow.SharedOutputContext;
import com.dtolabs.rundeck.core.execution.workflow.steps.PluginStepContextImpl;
import com.dtolabs.rundeck.core.resources.ResourceModelSource;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceFactory;
import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.core.plugins.configuration.Describable;
import com.dtolabs.rundeck.core.plugins.configuration.Description;
import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
import com.dtolabs.rundeck.core.plugins.configuration.PropertyUtil;
import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
import com.dtolabs.rundeck.plugins.PluginLogger;
import com.dtolabs.rundeck.plugins.step.PluginStepContext;
import com.dtolabs.rundeck.plugins.util.DescriptionBuilder;
import com.hashicorp.nomad.apimodel.Job;
import com.hashicorp.nomad.apimodel.JobListStub;
import com.hashicorp.nomad.apimodel.NodeListStub;
import com.hashicorp.nomad.javasdk.AgentApi;
import com.hashicorp.nomad.javasdk.JobsApi;
import com.hashicorp.nomad.javasdk.NodesApi;
import com.hashicorp.nomad.javasdk.NomadApiClient;
import com.hashicorp.nomad.javasdk.NomadApiConfiguration;
import com.hashicorp.nomad.javasdk.NomadException;
import com.hashicorp.nomad.javasdk.NomadResponse;
import com.hashicorp.nomad.javasdk.ServerQueryResponse;

import org.apache.log4j.Logger;

import java.util.List;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

@Plugin(service = "ResourceModelSource", name = "nomadNodes")
public class NomadResourceFactory implements ResourceModelSourceFactory, Describable {

    public static final Logger logger = Logger.getLogger(NomadResourceFactory.class);

    public static final String PROVIDER_NAME = "nomadNodes";
    public static final String PROVIDER_TITLE = "Nomad source";
    public static final String PROVIDER_DESCRIPTION = "Retrieve list of servers/agents and running jobs from a Nomad cluster";

    /**
     * Overriding this method gives the plugin a chance to take part in building the
     * {@link com.dtolabs.rundeck.core.plugins.configuration.Description} presented
     * by this plugin. This subclass can use the {@link DescriptionBuilder} to
     * modify all aspects of the description, add or remove properties, etc.
     */
    @Override
    public Description getDescription() {
        return DescriptionBuilder.builder().name(PROVIDER_NAME).title(PROVIDER_TITLE).description(PROVIDER_DESCRIPTION)
                .property(PropertyUtil.string("nomad_url", "Nomad Cluster URL", "scheme://fqdn:port", true, null, null,
                        null, null))
                .property(PropertyUtil.string("nomad_token_ro", "Nomad API RO Token", "API Read Only Token", true, null,
                        null, null, null))
                .property(PropertyUtil.string("nomad_token_path", "Nomad API Token Path",
                        "API Token Rundeck Storage Path", true, "keys/nomad/dev/acl_token", null, null, null))
                .property(PropertyUtil.string("cluster_name", "Cluster Name", null, true, null, null, null, null))
                .property(PropertyUtil.string("tags", "Extra Tags", null, false, null, null, null, null)).build();
    }

    /**
     * Here is the meat of the plugin implementation, which should perform the
     * appropriate logic for your plugin.
     */
    @Override
    public ResourceModelSource createResourceModelSource(final Properties properties) throws ConfigurationException {
        final Nomadmodel resource = new Nomadmodel(properties);
        return resource;
    }

    class Nomadmodel implements ResourceModelSource {

        private final Properties configuration;

        public Nomadmodel(Properties configuration) {
            this.configuration = configuration;
        }

        @Override
        public INodeSet getNodes() throws ResourceModelSourceException {

            final NodeSetImpl nodeSet = new NodeSetImpl();

            String nomadAuthToken = configuration.getProperty("nomad_token_ro");
            String nomadUrl = configuration.getProperty("nomad_url");
            String cluster_name = configuration.getProperty("cluster_name");
            String tags = configuration.getProperty("tags");

            // Nomad API call

            NomadApiConfiguration config = new NomadApiConfiguration.Builder().setAddress(nomadUrl)
                    .setAuthToken(nomadAuthToken).build();

            NomadApiClient apiClient = new NomadApiClient(config);
            logger.error("PONG!");

            try {
                // Populate NodeSet with Active Job references
                JobsApi jobsApi = apiClient.getJobsApi();
                ServerQueryResponse<List<JobListStub>> jobList = jobsApi.list();
                for (JobListStub job : jobList.getValue()) {
                    logger.error("Nomad Job: " + job.toString());
                    NodeEntryImpl node = new NodeEntryImpl();
                    if (null == node.getAttributes()) {
                        node.setAttributes(new HashMap<>());
                    }

                    node.setNodename(job.getName());
                    node.setAttribute("cluster", cluster_name);
                    node.setAttribute("id", job.getId());
                    node.setAttribute("parent_id", job.getParentId());
                    node.setAttribute("type", job.getType());
                    node.setAttribute("status", job.getStatus());
                    node.setAttribute("parametrized", Boolean.toString(job.getParameterizedJob()));
                    node.setAttribute("periodic", Boolean.toString(job.getPeriodic()));
                    node.setAttribute("priority", Integer.toString(job.getPriority()));
                    node.setAttribute("token_path", configuration.getProperty("nomad_token_path"));
                    node.setAttribute("unmapped", job.getUnmappedProperties().toString());

                    HashSet<String> tagset = new HashSet<>();
                    tagset.add("nomad_cluster_" + cluster_name);
                    tagset.add("nomad_job");
                    if (tags != null) {
                        tagset.add(tags);
                    }
                    node.setTags(tagset);
                    nodeSet.putNode(node);
                }

                logger.error("Checking nodes");
                // Populate NodeSet structure with Nomad node references
                NodesApi nodesApi = apiClient.getNodesApi();
                logger.error("Node List" + nodesApi.list().getRawEntity());
                ServerQueryResponse<List<NodeListStub>> nodeList = nodesApi.list();

                for (NodeListStub nomadNode : nodeList.getValue()) {
                    logger.error("Nomad Node: " + nomadNode.toString());
                    NodeEntryImpl node = new NodeEntryImpl();
                    if (null == node.getAttributes()) {
                        node.setAttributes(new HashMap<>());
                    }

                    node.setNodename(nomadNode.getName());
                    node.setAttribute("cluster", cluster_name);
                    node.setAttribute("id", nomadNode.getId());
                    node.setAttribute("status", nomadNode.getStatus());
                    node.setAttribute("class", nomadNode.getNodeClass());
                    node.setAttribute("datacenter", nomadNode.getDatacenter());
                    node.setAttribute("scheduling_elegibility", nomadNode.getSchedulingEligibility());
                    node.setAttribute("drain", Boolean.toString(nomadNode.getDrain()));
                    node.setAttribute("version", nomadNode.getVersion());
                    node.setAttribute("drivers", nomadNode.getDrivers().toString());
                    node.setAttribute("unmapped", nomadNode.getUnmappedProperties().toString());

                    HashSet<String> tagset = new HashSet<>();
                    tagset.add("nomad_cluster_" + cluster_name);
                    tagset.add("nomad_node");
                    if (tags != null) {
                        tagset.add(tags);
                    }
                    node.setTags(tagset);
                    nodeSet.putNode(node);
                }

            } catch (IOException e) {
                logger.error("IO Exception: " + e.getMessage());
                e.printStackTrace();
            } catch (NomadException e) {
                logger.error("Nomad Exception: " + e.getMessage());
                e.printStackTrace();

            }

            return nodeSet;
        }


    }

}                                                    