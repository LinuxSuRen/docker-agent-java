package io.jenkins.plugins.sample;

import hudson.console.ConsoleNote;
import hudson.model.TaskListener;
import io.jenkins.plugins.docker.agent.drivers.JavaDockerDriver;
import it.dockins.dockerslaves.spi.DockerHostConfig;
import org.jenkinsci.plugins.docker.commons.credentials.DockerServerEndpoint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

public class TestDriver
{
    static JavaDockerDriver driver = null;

    @BeforeClass
    public static void setup() throws IOException, InterruptedException
    {
        DockerServerEndpoint endpoint = new DockerServerEndpoint("tcp://192.168.1.135:2375", "");

        driver = new JavaDockerDriver(new DockerHostConfig(endpoint, null));
    }

    @Test
    public void pullImage() throws IOException, InterruptedException
    {
        driver.pullImage(new TaskListener(){

            @Override
            public PrintStream getLogger()
            {
                return System.out;
            }

            @Override
            public void annotate(ConsoleNote ann) throws IOException
            {

            }

            @Override
            public void hyperlink(String url, String text) throws IOException
            {

            }

            @Override
            public PrintWriter error(String msg)
            {
                return null;
            }

            @Override
            public PrintWriter error(String format, Object... args)
            {
                return null;
            }

            @Override
            public PrintWriter fatalError(String msg)
            {
                return null;
            }

            @Override
            public PrintWriter fatalError(String format, Object... args)
            {
                return null;
            }
        }, "alpine:2.7");
    }

    @Test
    public void launchRemotingContainer() throws IOException, InterruptedException
    {
        driver.launchRemotingContainer(null, "alpine:2.7", null, null);
    }
}