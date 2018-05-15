package io.jenkins.plugins.docker.agent.drivers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.api.model.Link;
import com.github.dockerjava.api.model.Version;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.netty.NettyDockerCmdExecFactory;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.TaskListener;
import io.jenkins.plugins.docker.agent.JenkinsPullImageResultCallback;
import it.dockins.dockerslaves.Container;
import it.dockins.dockerslaves.DockerComputer;
import it.dockins.dockerslaves.spec.Hint;
import it.dockins.dockerslaves.spi.DockerDriver;
import it.dockins.dockerslaves.spi.DockerHostConfig;

import java.io.IOException;
import java.util.List;

/**
 * @author suren
 */
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
        ListVolumesResponse volumeResponse = dockerClient.listVolumesCmd().exec();
        List<InspectVolumeResponse> volumes = volumeResponse.getVolumes();
        if(volumes != null)
        {
            for(InspectVolumeResponse inspectVolume : volumes)
            {
                if(inspectVolume.getName().equals(name))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String createVolume(TaskListener listener) throws IOException, InterruptedException
    {
        CreateVolumeResponse volumeResponse = dockerClient.createVolumeCmd().exec();

        return volumeResponse.getName();
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
        listener.getLogger().println("workDir:" + workdir);

        if(!checkImageExists(listener, image))
        {
            // TODO it should be sync operation here
            pullImage(listener, image);
        }

        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(image)
                .exec();
        dockerClient.startContainerCmd(containerResponse.getId()).exec();

        com.github.dockerjava.api.model.Container container = findContainer(containerResponse.getId());
        return new Container(image, containerResponse.getId());
    }

    @Override
    public Container launchBuildContainer(TaskListener listener, String image, Container remotingContainer, List<Hint> hints) throws IOException, InterruptedException
    {
        return null;
    }

    @Override
    public Container launchSideContainer(TaskListener listener, String image, Container remotingContainer, List<Hint> hints) throws IOException, InterruptedException
    {
        String remotingContainerId = remotingContainer.getId();

        com.github.dockerjava.api.model.Container reContainer = findContainer(remotingContainerId);

        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(image)
//                .withVolumesFrom(reContainer.get)
                .withLinks(new Link(remotingContainerId, remotingContainerId))
                .exec();
        dockerClient.startContainerCmd(containerResponse.getId()).exec();

        return new Container(image, containerResponse.getId());
    }

    @Override
    public Proc execInContainer(TaskListener listener, String containerId, Launcher.ProcStarter starter) throws IOException, InterruptedException
    {
        ExecCreateCmdResponse cmd = dockerClient.execCreateCmd(containerId).withCmd(starter.cmds().toArray(new String[]{})).exec();

        return null;
    }

    @Override
    public void removeContainer(TaskListener listener, Container instance) throws IOException, InterruptedException
    {
        dockerClient.removeContainerCmd(instance.getImageName())
                .withContainerId(instance.getId()).exec();
    }

    @Override
    public void pullImage(final TaskListener listener, final String image) throws IOException, InterruptedException
    {
        dockerClient.pullImageCmd(image)
//                .withRepository("http://hub.docker.com")
//                .withTag("latest")
                .exec(new JenkinsPullImageResultCallback(listener, image)).awaitSuccess();
        //8357 3416
//        dockerClient.pullImageCmd(image).exec(new ResultCallback(){
//
//            @Override
//            public void close() throws IOException
//            {
//            }
//
//            @Override
//            public void onStart(Closeable closeable)
//            {
//                listener.getLogger().println("start pulling image : " + image);
//            }
//
//            @Override
//            public void onNext(Object o)
//            {
//                listener.getLogger().println(o);
//            }
//
//            @Override
//            public void onError(Throwable throwable)
//            {
//                listener.error(throwable.getMessage());
//            }
//
//            @Override
//            public void onComplete()
//            {
//                listener.getLogger().println("complete pulling image : " + image);
//            }
//        });
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
        Version version = dockerClient.versionCmd().exec();

        return version.getVersion();
    }

    @Override
    public void close() throws IOException
    {
        dockerClient.close();
    }
}