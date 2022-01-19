package com.dalzai.findmyexpert;

public class VideoModel
{
    private String video_id;
    private String video_title;
    private String video_thumbnail;
    private String channel_title;

    public VideoModel(String video_id, String video_title, String video_thumbnail, String channel_title) {
        this.video_id = video_id;
        this.video_title = video_title;
        this.video_thumbnail = video_thumbnail;
        this.channel_title = channel_title;
    }

    public String getVideo_id() {
        return video_id;
    }

    public void setVideo_id(String video_id) {
        this.video_id = video_id;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
    }

    public String getChannel_title() {
        return channel_title;
    }

    public void setChannel_title(String channel_title) {
        this.channel_title = channel_title;
    }
}
