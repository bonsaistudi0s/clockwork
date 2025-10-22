package dev.xylonity.bonsai.clockwork.client.sound.handler;

public interface SoundHandler<T> {

    void tick(T entity);
    void stopAll(T entity);
    void clear();
    boolean canHandle(Object entity);

}