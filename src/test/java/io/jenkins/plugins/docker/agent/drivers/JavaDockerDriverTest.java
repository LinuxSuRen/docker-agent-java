package io.jenkins.plugins.docker.agent.drivers;

import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import it.dockins.dockerslaves.Container;
import it.dockins.dockerslaves.spi.DockerHostConfig;
import org.jenkinsci.plugins.docker.commons.credentials.DockerServerEndpoint;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

/**
 * @author suren
 */
public class JavaDockerDriverTest
{
    static JavaDockerDriver driver;
    static TaskListener listener;

    String alpine = "alpine:2.7";

//    @Rule
//    public JenkinsRule jenkins = new JenkinsRule();

    @BeforeClass
    public static void setup() throws IOException, InterruptedException
    {
        DockerServerEndpoint endpoint = new DockerServerEndpoint("tcp://surenpi.com:5679", "");

        driver = new JavaDockerDriver(new DockerHostConfig(endpoint, null));

        listener = new StreamTaskListener(System.out);
    }

    @Test
    public void hasVolume() throws IOException, InterruptedException
    {
        String volume = "volume";

        Assert.assertFalse(driver.hasVolume(listener, volume));
    }

    @Test
    public void createVolume() throws IOException, InterruptedException
    {
        Assert.assertNotNull(driver.createVolume(listener));
    }

    @Test
    public void hasContainer() throws IOException, InterruptedException
    {
        String notExistsId = "notExistsId";

        Assert.assertFalse(driver.hasContainer(listener, notExistsId));
    }

    @Test
    public void launchRemotingContainer() throws IOException, InterruptedException
    {
        driver.launchRemotingContainer(listener, "alpine:2.7", null, null);
    }

    @Test
    public void execInContainer() throws IOException, InterruptedException
    {
        String containerId = "";
        driver.execInContainer(listener, containerId, null);
    }

    @Test
    public void removeContainer() throws IOException, InterruptedException
    {
        Container container = new Container("dramaturg/apache-openmeetings", "6d6fccbd31d9");
        driver.removeContainer(listener, container);
    }

    @Test
    public void pullImage() throws IOException, InterruptedException
    {
        driver.pullImage(listener, "alpine");
    }

    @Test
    public void checkImageExists() throws IOException, InterruptedException
    {
        Assert.assertTrue(driver.checkImageExists(listener, "mysql:5.6"));
    }

    @Test
    public void serverVersion() throws IOException, InterruptedException
    {
        String version = driver.serverVersion(listener);
        Assert.assertNotNull(version);
    }
}