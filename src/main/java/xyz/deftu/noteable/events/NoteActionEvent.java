package xyz.deftu.noteable.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import xyz.deftu.noteable.notes.Note;

public interface NoteActionEvent {
    Event<NoteActionEvent> EVENT = EventFactory.createArrayBacked(NoteActionEvent.class, (listeners) -> (action, note) -> {
        for (NoteActionEvent listener : listeners) {
            listener.onNoteAction(action, note);
        }
    });

    void onNoteAction(NoteAction action, Note note);

    enum NoteAction {
        CREATE,
        REMOVE
    }
}
