package io.jenkins.plugins.docker.agent.drivers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.SearchImagesCmd;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.SearchItem;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import it.dockins.dockerslaves.Container;
import it.dockins.dockerslaves.DockerComputer;
import it.dockins.dockerslaves.spec.Hint;
import it.dockins.dockerslaves.spi.DockerDriver;
import it.dockins.dockerslaves.spi.DockerHostConfig;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class JavaDockerDriver extends DockerDriver
{
    private DockerClientConfig config;
    private DockerCmdExecFactory dockerCmdExecFactory;
    private DockerClient dockerClient;

    public JavaDockerDriver(DockerHostConfig dockerHost)
    {
        config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(dockerHost.getEndpoint().getUri())
                .withDockerTlsVerify(false)
                .build();

        dockerCmdExecFactory = new NettyDockerCmdExecFactory()
//                .withReadTimeout(1000)
                .withConnectTimeout(1000);
//                .withMaxTotalConnections(100)
//                .withMaxPerRouteConnections(10);

        dockerClient = DockerClientBuilder.getInstance(config)
                .withDockerCmdExecFactory(dockerCmdExecFactory)
                .build();
    }

    @Override
    public boolean hasVolume(TaskListener listener, String name) throws IOException, InterruptedException
    {
        return false;
    }

    @Override
    public String createVolume(TaskListener listener) throws IOException, InterruptedException
    {
        return null;
    }

    @Override
    public boolean hasContainer(TaskListener listener, String id) throws IOException, InterruptedException
    {
        return findContainer(id) != null;
    }

    private com.github.dockerjava.api.model.Container findContainer(String id)
    {
        List<com.github.dockerjava.api.model.Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        if(containers == null)
        {
            return null;
        }

        for(com.github.dockerjava.api.model.Container container : containers)
        {
            if(container.getId().equals(id))
            {
                return container;
            }
        }

        return null;
    }

    @Override
    public Container launchRemotingContainer(TaskListener listener, String image, String workdir, DockerComputer computer) throws IOException, InterruptedException
    {
        if(checkImageExists(listener, image))
        {
            List<Image> images = dockerClient.listImagesCmd().exec();

            List<SearchItem> cmds = dockerClient.searchImagesCmd(image).exec();
            if(cmds != null && cmds.size() > 0)
            {
                System.out.println(cmds.get(0));

//                dockerClient.execStartCmd(cmds.get(0).getName());
            }

            CreateContainerResponse containerResponse = dockerClient.createContainerCmd(image)
                    .withCmd("echo 1").exec();
            dockerClient.startContainerCmd(containerResponse.getId()).exec();

            com.github.dockerjava.api.model.Container container = findContainer(containerResponse.getId());
            return new Container(image, containerResponse.getId());
        }

        return null;
    }

    @Override
    public Container launchBuildContainer(TaskListener listener, String image, Container remotingContainer, List<Hint> hints) throws IOException, InterruptedException
    {
        return null;
    }

    @Override
    public Container launchSideContainer(TaskListener listener, String image, Container remotingContainer, List<Hint> hints) throws IOException, InterruptedException
    {
        return null;
    }

    @Override
    public Proc execInContainer(TaskListener listener, String containerId, Launcher.ProcStarter starter) throws IOException, InterruptedException
    {
        return null;
    }

    @Override
    public void removeContainer(TaskListener listener, Container instance) throws IOException, InterruptedException
    {

    }

    @Override
    public void pullImage(final TaskListener listener, final String image) throws IOException, InterruptedException
    {
        dockerClient.pullImageCmd(image).exec(new ResultCallback(){

            @Override
            public void close() throws IOException
            {
            }

            @Override
            public void onStart(Closeable closeable)
            {
                listener.getLogger().println("start pulling image : " + image);
            }

            @Override
            public void onNext(Object o)
            {
                listener.getLogger().println(o);
            }

            @Override
            public void onError(Throwable throwable)
            {

            }

            @Override
            public void onComplete()
            {
                listener.getLogger().println("complete pulling image : " + image);
            }
        });
    }

    @Override
    public boolean checkImageExists(TaskListener listener, String image) throws IOException, InterruptedException
    {
        List<Image> images = dockerClient.listImagesCmd().withImageNameFilter(image).exec();
        return (images != null && images.size() > 0);
    }

    @Override
    public void buildDockerfile(TaskListener listener, String dockerfilePath, String tag, boolean pull) throws IOException, InterruptedException
    {

    }

    @Override
    public String serverVersion(TaskListener listener) throws IOException, InterruptedException
    {
        return null;
    }

    @Override
    public void close() throws IOException
    {
    }
}