package io.jenkins.plugins.docker.agent.drivers;

import hudson.Extension;
import hudson.model.Job;
import it.dockins.dockerslaves.DefaultDockerHostSource;
import it.dockins.dockerslaves.spi.DockerDriver;
import it.dockins.dockerslaves.spi.DockerDriverFactory;
import it.dockins.dockerslaves.spi.DockerDriverFactoryDescriptor;
import it.dockins.dockerslaves.spi.DockerHostSource;
import org.jenkinsci.plugins.docker.commons.credentials.DockerServerEndpoint;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;

public class JavaDockerAPIDockerDriverFactory extends DockerDriverFactory
{

    private final DockerHostSource dockerHostSource;

    @DataBoundConstructor
    public JavaDockerAPIDockerDriverFactory(DockerHostSource dockerHostSource) {
        this.dockerHostSource = dockerHostSource;
    }

    public JavaDockerAPIDockerDriverFactory(DockerServerEndpoint dockerHost) {
        this(new DefaultDockerHostSource(dockerHost));
    }

    public DockerHostSource getDockerHostSource() {
        return dockerHostSource;
    }

    @Override
    public DockerDriver forJob(Job context) throws IOException, InterruptedException
    {
        return new JavaDockerDriver(dockerHostSource.getDockerHost(context));
    }

    @Extension
    public static class DescriptorImp extends DockerDriverFactoryDescriptor
    {

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Docker Java API";
        }
    }
}