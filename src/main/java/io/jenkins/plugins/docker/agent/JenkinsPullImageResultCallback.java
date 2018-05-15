package io.jenkins.plugins.docker.agent;

import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.command.PullImageResultCallback;
import hudson.model.TaskListener;

import java.io.Closeable;

/**
 * @author suren
 */
public class JenkinsPullImageResultCallback extends PullImageResultCallback
{
    private final TaskListener listener;
    private final JenkinsProgress progress;
    private String image;

    public JenkinsPullImageResultCallback(TaskListener listener, String image)
    {
        this.listener = listener;
        this.image = image;
        progress = new JenkinsProgress(listener);
    }

    @Override
    public void onNext(PullResponseItem item)
    {
        super.onNext(item);

        progress.next(item);
    }

    @Override
    public void onStart(Closeable stream)
    {
        super.onStart(stream);

        listener.getLogger().println("Start pulling image: " + image);
    }

    @Override
    public void onComplete()
    {
        super.onComplete();

        listener.getLogger().println("Complete pulling image: " + image);
    }
}
