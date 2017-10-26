package io.github.valfadeev.rundeck_nomad_plugin.nomad;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.hashicorp.nomad.apimodel.Job;
import org.junit.Test;
import utils.TestConfigurationMapBuilder;

import static io.github.valfadeev.rundeck_nomad_plugin.nomad.NomadConfigOptions.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

public class NomadJobProviderTest {
    
    @Test
    public void shouldGenerateJobConfig() throws Exception {
        final Map<String, Object> config = TestConfigurationMapBuilder.builder()
                .addItem(NOMAD_URL, "http://localhost:4646")
                .addItem(NOMAD_DATACENTER, "")
                .addItem(NOMAD_REGION, "")
                .addItem(NOMAD_GROUP_COUNT, "3")
                .addItem(NOMAD_JOB_TYPE, "service")
                .addItem(NOMAD_MAX_FAIL_PCT, "0")
                .addItem(NOMAD_ENV_VARS, "FOO=BAR")
                .addItem(NOMAD_TASK_CPU, "50")
                .addItem(NOMAD_TASK_MEMORY, "512")
                .addItem(NOMAD_TASK_IOPS, "100")
                .addItem(NOMAD_NETWORK_BANDWIDTH, "10")
                .addItem(NOMAD_DYNAMIC_PORTS, "http,https")
                .addItem(NOMAD_RESERVED_PORTS, "amqp=5672\ndb=6379")
                .addItem(NOMAD_MAX_PARALLEL, "1")
                .addItem(NOMAD_HEALTH_CHECK, "")
                .addItem(NOMAD_MIN_HEALTHY_TIME, "")
                .addItem(NOMAD_HEALTHY_DEADLINE, "")
                .addItem(NOMAD_AUTO_REVERT, "")
                .addItem(NOMAD_CANARY, "")
                .addItem(NOMAD_STAGGER, "")
                .getConfig();

        final Map<String, Object> agentConfig = new HashMap<>();
        agentConfig.put("Datacenter", "dc1");
        agentConfig.put("Region", "global");

        final Map<String, Object> taskConfig = new HashMap<>();


        final Job job = NomadJobProvider.getJob(
                config,
                agentConfig,
                taskConfig,
                "docker",
                "testId",
                "testName",
                "rundeck");

        assertThat(job.getDatacenters(), is(Arrays.asList(new String[]{"dc1"})));
        assertThat(job.getId(), is("testId"));
        assertThat(job.getName(), is("testName"));
        assertThat(job.getTaskGroups().get(0).getName(), is("rundeck"));
        assertThat(job.getRegion(), is("global"));
        assertThat(job.getType(), is("service"));
        assertThat(job.getUpdate().getMaxParallel(), is(1));
    }

}