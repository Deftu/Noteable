package xyz.deftu.noteable.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import xyz.deftu.noteable.notes.Note;

import java.util.List;

@FunctionalInterface
public interface NoteConfigureEvent {
    Event<NoteConfigureEvent> EVENT = EventFactory.createArrayBacked(NoteConfigureEvent.class, (listeners) -> (notes) -> {
        for (NoteConfigureEvent listener : listeners) {
            listener.onNoteConfigure(notes);
        }
    });

    void onNoteConfigure(List<Note> notes);
}
