package io.jenkins.plugins.docker.agent;

import com.github.dockerjava.api.model.BuildResponseItem;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import hudson.model.TaskListener;

import java.io.Closeable;

/**
 * @author suren
 */
public class JenkinsBuildImageResultCallback extends BuildImageResultCallback
{
    private final TaskListener listener;
    private JenkinsProgress progress;

    public JenkinsBuildImageResultCallback(TaskListener listener)
    {
        this.listener = listener;
        progress = new JenkinsProgress(listener);
    }

    @Override
    public void onComplete()
    {
        super.onComplete();

        listener.getLogger().println("Complete building image.");
    }

    @Override
    public void onStart(Closeable stream)
    {
        super.onStart(stream);

        listener.getLogger().println("Start building image.");
    }

    @Override
    public void onNext(BuildResponseItem item)
    {
        super.onNext(item);

        progress.next(item);
    }
}
