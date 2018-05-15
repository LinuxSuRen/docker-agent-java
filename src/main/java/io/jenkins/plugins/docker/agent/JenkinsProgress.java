package io.jenkins.plugins.docker.agent;

import com.github.dockerjava.api.model.ResponseItem;
import hudson.model.TaskListener;

public class JenkinsProgress
{
    private final TaskListener listener;

    public JenkinsProgress(TaskListener listener)
    {
        this.listener = listener;
    }

    public void next(ResponseItem item)
    {
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
}
