package dev.aimusic.backend.task.dao;

public enum TaskType {
    TEXT_TO_MUSIC,       // 文本生成音乐
    MUSIC_EDITING,       // 音乐编辑（音乐+文本生成音乐）
    VIDEO_SOUNDTRACK     // 视频配乐（视频+文本生成音乐）
}