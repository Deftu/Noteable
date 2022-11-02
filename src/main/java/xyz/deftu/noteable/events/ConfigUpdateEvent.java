package xyz.deftu.noteable.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import xyz.deftu.noteable.config.Config;

@FunctionalInterface
public interface ConfigUpdateEvent {
    Event<ConfigUpdateEvent> EVENT = EventFactory.createArrayBacked(ConfigUpdateEvent.class, (listeners) -> (config) -> {
        for (ConfigUpdateEvent listener : listeners) {
            listener.onConfigUpdate(config);
        }
    });

    void onConfigUpdate(Config config);
}
