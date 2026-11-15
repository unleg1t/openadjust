package com.adjustclient.ui.dialog;

import com.adjustclient.Adjust;
import com.adjustclient.render.font.Fonts;
import com.adjustclient.render.font.TextInput;
import com.adjustclient.render.font.TextInputBox;
import com.adjustclient.util.ChatMessage;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class DialogBuilder {
    public static final Function<DialogScreen, Boolean> ALWAYS_ENABLED = dialog -> true;
    public static final AlphaModifier IDENTITY_ALPHA = alpha -> alpha;
    public String title;
    public ChatMessage message = new ChatMessage();
    public List<TextInputElement> textInputs = new ArrayList<>();
    public List<AdjustButton> buttons = new ArrayList<>();
    public Int2ObjectOpenHashMap<Consumer<DialogScreen>> actions = new Int2ObjectOpenHashMap<>();
    public Map<AdjustButton, Function<DialogScreen, Boolean>> validators = new HashMap<>();
    public List<DropdownElement> dropdowns = new ArrayList<>();
    public int bottomInset;
    public Object field36;

    public DialogBuilder(String title) { this.title = title; }

    public DialogBuilder addDropdown(String label, String[] options) {
        dropdowns.add(new DropdownElement(label, options));
        return this;
    }
    public DialogBuilder addDropdown(String label, List<?> options, Object onChange) {
        String[] labels = options.stream().map(String::valueOf).toArray(String[]::new);
        DropdownElement dropdown = new DropdownElement(label, labels);
        if (onChange != null) dropdown.selectionListener = (java.util.function.BiConsumer<Integer, Integer>) onChange;
        dropdowns.add(dropdown);
        return this;
    }
    public DialogBuilder addButton(String text, Consumer<DialogScreen> action, Function<DialogScreen, Boolean> enabled) {
        int id = buttons.size();
        AdjustButton button = new AdjustButton(id, 0.0F, 0.0F,
                (int) (Fonts.TAHOMA_12.getStringWidth(text) + 6.0F), 10, text, IDENTITY_ALPHA);
        button.base = 62;
        buttons.add(button);
        actions.put(id, action);
        validators.put(button, enabled);
        return this;
    }
    public DialogBuilder addTextInput(String label, String text, int maxLength, int width) {
        int index = textInputs.size();
        TextInput input = new TextInput(label, Fonts.TAHOMA_12, maxLength);
        input.field.setText(text);
        textInputs.add(new TextInputElement(new TextInputBox(input), index, width));
        return this;
    }
    public DialogScreen build() {
        if (!textInputs.isEmpty()) {
            textInputs.get(0).box.input.focused = true;
            net.minecraft.client.settings.KeyBinding.unPressAllKeys();
        }
        DialogScreen dialog = new DialogScreen(title, 70.0F, message.getComponent(),
                textInputs, buttons, actions, validators, dropdowns, bottomInset);
        Adjust.INSTANCE.screenStack.addOverlay(dialog);
        return dialog;
    }
    public DialogScreen build(float width) {
        if (!textInputs.isEmpty()) {
            textInputs.get(0).box.input.focused = true;
            net.minecraft.client.settings.KeyBinding.unPressAllKeys();
        }
        DialogScreen dialog = new DialogScreen(title, width, message.getComponent(),
                textInputs, buttons, actions, validators, dropdowns, bottomInset);
        Adjust.INSTANCE.screenStack.addOverlay(dialog);
        return dialog;
    }
}
