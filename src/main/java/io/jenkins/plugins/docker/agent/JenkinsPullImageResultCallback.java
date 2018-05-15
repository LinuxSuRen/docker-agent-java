package io.jenkins.plugins.docker.agent;

import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.ResponseItem;
import com.github.dockerjava.core.command.PullImageResultCallback;
import hudson.model.TaskListener;

import java.io.Closeable;

/**
 * @author suren
 */
public class JenkinsPullImageResultCallback extends PullImageResultCallback
{
    private final TaskListener listener;
    private String image;

    public JenkinsPullImageResultCallback(TaskListener listener, String image)
    {
        this.listener = listener;
        this.image = image;
    }

    @Override
    public void onNext(PullResponseItem item)
    {
        super.onNext(item);

        String from = item.getFrom();
        ResponseItem.ProgressDetail progress = item.getProgressDetail();
        if(progress == null || progress.getTotal() == null)
        {
            listener.getLogger().println(item.getStatus());
            return;
        }

        long current = progress.getCurrent();
        long total = progress.getTotal();
        long percent = current / total * 100;

        listener.getLogger().println("From: " + from + " percent: " + percent);
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
